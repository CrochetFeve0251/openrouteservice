/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package heigit.ors.routing.graphhopper.extensions.core;

import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectMap;
import com.graphhopper.coll.GHIntObjectHashMap;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.BeelineWeightApproximator;
import com.graphhopper.routing.weighting.ConsistentWeightApproximator;
import com.graphhopper.routing.weighting.WeightApproximator;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.SPTEntry;
import com.graphhopper.util.*;
import heigit.ors.util.DebugUtility;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.PriorityQueue;

/**
 * Calculates best path using core routing algorithm.
 *
 * @author Andrzej Oles
 */

public class CoreALT extends AbstractCoreRoutingAlgorithm {
    private static final Logger LOGGER = Logger.getLogger(AbstractCoreRoutingAlgorithm.class.getName());
    protected AStarEntry currFrom;
    protected AStarEntry currTo;
    protected IntObjectMap<AStarEntry> bestWeightMapFrom;
    protected IntObjectMap<AStarEntry> bestWeightMapTo;
    private IntObjectMap<AStarEntry> bestWeightMapOther;
    private ConsistentWeightApproximator weightApprox;
    private IntHashSet ignoreExplorationFrom = new IntHashSet();
    private IntHashSet ignoreExplorationTo = new IntHashSet();
    
    private PriorityQueue<AStarEntry> pqCHFrom;
    private PriorityQueue<AStarEntry> pqCHTo;
    private PriorityQueue<AStarEntry> pqCoreFrom;
    private PriorityQueue<AStarEntry> pqCoreTo;

    SPTEntry fromProxy;
    SPTEntry toProxy;

    int visitedCountProxy;

    public CoreALT(Graph graph, Weighting weighting, TraversalMode tMode, double maxSpeed) {
        super(graph, weighting, tMode, maxSpeed);
        BeelineWeightApproximator defaultApprox = new BeelineWeightApproximator(nodeAccess, weighting);
        defaultApprox.setDistanceCalc(Helper.DIST_PLANE);
        setApproximation(defaultApprox);
    }

    @Override
    protected void initCollections(int size) {
        pqCHFrom = new PriorityQueue<AStarEntry>(size);
        bestWeightMapFrom = new GHIntObjectHashMap<AStarEntry>(size);

        pqCHTo = new PriorityQueue<AStarEntry>(size);
        bestWeightMapTo = new GHIntObjectHashMap<AStarEntry>(size);

        pqCoreFrom = new PriorityQueue<>(size);
        pqCoreTo = new PriorityQueue<>(size);
    }

    /**
     * @param approx if true it enables approximate distance calculation from lat,lon values
     */
    public CoreALT setApproximation(WeightApproximator approx) {
        weightApprox = new ConsistentWeightApproximator(approx);
        return this;
    }

    public WeightApproximator getApproximation() {
        return weightApprox.getApproximation();
    }

    @Override
    protected SPTEntry createSPTEntry(int node, double weight) {
        throw new IllegalStateException("use AStarEdge constructor directly");
    }

    @Override
    public void initFrom(int from, double weight) {
        currFrom = new AStarEntry(EdgeIterator.NO_EDGE, from, weight, weight);
        pqCHFrom.add(currFrom);
        fromProxy = getProxyNode(from, false);

        // FIXME: debug info
        if (DebugUtility.isDebug())
            System.out.println("getProxyNode(from) " + fromProxy);

        if (!traversalMode.isEdgeBased()) {
            bestWeightMapFrom.put(from, currFrom);
            if (currTo != null) {
                bestWeightMapOther = bestWeightMapTo;
                updateBestPath(GHUtility.getEdge(graph, from, currTo.adjNode), currTo, from);
            }
        } else if (currTo != null && currTo.adjNode == from) {
            // special case of identical start and end
            bestPath.setSPTEntry(currFrom);
            bestPath.setSPTEntryTo(currTo);
            finishedFrom = true;
            finishedTo = true;
        }
    }

    @Override
    public void initTo(int to, double weight) {
        currTo = new AStarEntry(EdgeIterator.NO_EDGE, to, weight, weight);
        pqCHTo.add(currTo);
        toProxy = getProxyNode(to, true);

        // FIXME: debug info
        if (DebugUtility.isDebug())
            System.out.println("getProxyNode(to) " + toProxy);

        if (!traversalMode.isEdgeBased()) {
            bestWeightMapTo.put(to, currTo);
            if (currFrom != null) {
                bestWeightMapOther = bestWeightMapFrom;
                updateBestPath(GHUtility.getEdge(graph, currFrom.adjNode, to), currFrom, to);
            }
        } else if (currFrom != null && currFrom.adjNode == to) {
            // special case of identical start and end
            bestPath.setSPTEntry(currFrom);
            bestPath.setSPTEntryTo(currTo);
            finishedFrom = true;
            finishedTo = true;
        }
    }

    @Override
    public boolean fillEdgesFrom() {
        if (pqCHFrom.isEmpty())
            return false;

        currFrom = pqCHFrom.poll();

        if (!inCore && chGraph.getLevel(currFrom.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            pqCoreFrom.add(currFrom);
        }
        else {
            bestWeightMapOther = bestWeightMapTo;
            fillEdges(currFrom, pqCHFrom, bestWeightMapFrom, outEdgeExplorer, false);
            visitedCountFrom1++;
        }

        return true;
    }

    @Override
    public boolean fillEdgesTo() {
        if (pqCHTo.isEmpty())
            return false;

        currTo = pqCHTo.poll();

        if (!inCore && chGraph.getLevel(currTo.adjNode) == coreNodeLevel) {
            // core entry point, do not relax its edges
            pqCoreTo.add(currTo);
        }
        else {
            bestWeightMapOther = bestWeightMapFrom;
            fillEdges(currTo, pqCHTo, bestWeightMapTo, inEdgeExplorer, true);
            visitedCountTo1++;
        }

        return true;
    }

    @Override
    public boolean finishedPhase1() {
        // we need to finish BOTH searches for CH!
        if (finishedFrom && finishedTo)
            return true;

        double fromWeight = currFrom.weight;
        double toWeight = currTo.weight;

        // changed also the final finish condition for CH
        if (!pqCoreFrom.isEmpty())
            fromWeight = Math.min(pqCoreFrom.peek().weight, fromWeight);
        if (!pqCoreTo.isEmpty())
            toWeight = Math.min(pqCoreTo.peek().weight, toWeight);


        return fromWeight >= bestPath.getWeight() && toWeight >= bestPath.getWeight();
    }

    @Override
    void runPhase2() {
        // re-init queues
        finishedFrom = pqCoreFrom.isEmpty();
        finishedTo = pqCoreTo.isEmpty();

        if (!finishedFrom && !finishedTo) {
            // If proxy node not set use the closest core entry point
            weightApprox.setFrom(fromProxy != null ? fromProxy.adjNode : pqCoreFrom.peek().adjNode);
            weightApprox.setTo(toProxy != null ? toProxy.adjNode : pqCoreTo.peek().adjNode);

            // TODO: take into account fromProxy.getWeightOfVisitedPath() and toProxy.getWeightOfVisitedPath()

            // FIXME: debug info
            if (DebugUtility.isDebug()) {
                System.out.println("pqCoreFrom.peek().adjNode " + pqCoreFrom.peek().adjNode);
                System.out.println("pqCoreTo.peek().adjNode " + pqCoreTo.peek().adjNode);
            }

            // copy into temporary array to avoid pointer change of PQ
            AStarEntry[] entriesFrom;
            AStarEntry[] entriesTo;

            entriesFrom = pqCoreFrom.toArray(new AStarEntry[pqCoreFrom.size()]);
            entriesTo = pqCoreTo.toArray(new AStarEntry[pqCoreTo.size()]);
            pqCoreFrom.clear();
            for (AStarEntry valueFrom : entriesFrom) {
                valueFrom.weight = valueFrom.weightOfVisitedPath + weightApprox.approximate(valueFrom.adjNode, false);
                pqCoreFrom.add(valueFrom);
//                for (AStarEntry valueTo : entriesTo) {
//                    if (!traversalMode.isEdgeBased()) {
//                        bestWeightMapFrom.put(valueFrom.adjNode, valueFrom);
//                        if (valueTo != null) {
//                            bestWeightMapOther = bestWeightMapTo;
//                            updateBestPathALT(GHUtility.getEdge(graph, valueFrom.adjNode, valueTo.adjNode), valueTo, valueFrom.adjNode);
//                        }
//                    }
//                }
            }
            currFrom = pqCoreFrom.peek();



//            entries = pqCoreTo.toArray(new AStarEntry[pqCoreTo.size()]);
            pqCoreTo.clear();
            for (AStarEntry valueTo : entriesTo) {
                valueTo.weight = valueTo.weightOfVisitedPath + weightApprox.approximate(valueTo.adjNode, true);
                pqCoreTo.add(valueTo);
//                for (AStarEntry valueFrom : entriesFrom) {
//                    if (!traversalMode.isEdgeBased()) {
//                        bestWeightMapTo.put(valueTo.adjNode, valueTo);
//                        if (valueFrom != null) {
//                            bestWeightMapOther = bestWeightMapFrom;
//                            updateBestPathALT(GHUtility.getEdge(graph, valueTo.adjNode, valueFrom.adjNode), valueFrom, valueTo.adjNode);
//                        }
//                    }
//                }
            }
            currTo = pqCoreTo.peek();
        }

        while (!finishedPhase2() && !isMaxVisitedNodesExceeded()) {
            if (!finishedFrom)
                finishedFrom = !fillEdgesFromALT();

            if (!finishedTo)
                finishedTo = !fillEdgesToALT();
        }

        // FIXME: debug info
        LOGGER.info("PHASE_1: " + getVisitedNodesPhase1() + "; PHASE_2: " + getVisitedNodesPhase2() + "; PROXY: " + getVisitedNodesProxy());
    }

    @Override
    public boolean finishedPhase2() {
        if (finishedFrom || finishedTo)
            return true;
//        return false;
        // using 'weight' is important and correct here e.g. approximation can get negative and smaller than 'weightOfVisitedPath'
        return currFrom.weight + currTo.weight >= bestPath.getWeight();
    }

    void fillEdges(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueue, IntObjectMap<AStarEntry> bestWeightMap,
                   EdgeExplorer explorer, boolean reverse) {
        EdgeIterator iter = explorer.setBaseNode(currEdge.adjNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;

            int traversalId = traversalMode.createTraversalId(iter, reverse);
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double tmpWeight = weighting.calcWeight(iter, reverse, currEdge.originalEdge) + currEdge.weight;
            if (Double.isInfinite(tmpWeight))
                continue;
            AStarEntry ee = bestWeightMap.get(traversalId);
            if (ee == null) {
                ee = new AStarEntry(iter.getEdge(), iter.getAdjNode(), tmpWeight, tmpWeight);
                // Modification by Maxim Rylov: Assign the original edge id.
                ee.originalEdge = iter.getOriginalEdge();
                ee.parent = currEdge;
                bestWeightMap.put(traversalId, ee);
                prioQueue.add(ee);
            } else if (ee.weight > tmpWeight) {
                prioQueue.remove(ee);
                ee.edge = iter.getEdge();
                ee.weight = tmpWeight;
                ee.weightOfVisitedPath = tmpWeight;
                ee.parent = currEdge;
                prioQueue.add(ee);
            } else
                continue;

            if (updateBestPath)
                updateBestPath(iter, ee, traversalId);
        }
    }

    boolean fillEdgesFromALT() {
        if (pqCoreFrom.isEmpty())
            return false;

        currFrom = pqCoreFrom.poll();
        bestWeightMapOther = bestWeightMapTo;
        fillEdgesALT(currFrom, pqCoreFrom, bestWeightMapFrom, ignoreExplorationFrom, outEdgeExplorer, false);
        visitedCountFrom2++;
        return true;
    }

    boolean fillEdgesToALT() {
        if (pqCoreTo.isEmpty())
            return false;

        currTo = pqCoreTo.poll();
        bestWeightMapOther = bestWeightMapFrom;
        fillEdgesALT(currTo, pqCoreTo, bestWeightMapTo, ignoreExplorationTo, inEdgeExplorer, true);
        visitedCountTo2++;
        return true;
    }

    private void fillEdgesALT(AStarEntry currEdge, PriorityQueue<AStarEntry> prioQueueOpenSet,
                           IntObjectMap<AStarEntry> bestWeightMap, IntHashSet ignoreExploration, EdgeExplorer explorer,
                           boolean reverse) {

        int currNode = currEdge.adjNode;
        EdgeIterator iter = explorer.setBaseNode(currNode);
        while (iter.next()) {
            if (!accept(iter, currEdge.edge))
                continue;

            int neighborNode = iter.getAdjNode();
            int traversalId = traversalMode.createTraversalId(iter, reverse);
            if (ignoreExploration.contains(traversalId))
                continue;

            // TODO performance: check if the node is already existent in the opposite direction
            // then we could avoid the approximation as we already know the exact complete path!
            // Modification by Maxim Rylov: use originalEdge as the previousEdgeId
            double alreadyVisitedWeight = weighting.calcWeight(iter, reverse, currEdge.originalEdge)
                    + currEdge.getWeightOfVisitedPath();
            if (Double.isInfinite(alreadyVisitedWeight))
                continue;

            AStarEntry ase = bestWeightMap.get(traversalId);
            if (ase == null || ase.getWeightOfVisitedPath() > alreadyVisitedWeight) {
                double currWeightToGoal = weightApprox.approximate(neighborNode, reverse);
                double estimationFullWeight = alreadyVisitedWeight + currWeightToGoal;
                if (ase == null) {
                    ase = new AStarEntry(iter.getEdge(), neighborNode, estimationFullWeight, alreadyVisitedWeight);
                    // Modification by Maxim Rylov: assign originalEdge
                    ase.originalEdge = iter.getOriginalEdge();
                    bestWeightMap.put(traversalId, ase);
                } else {
                    //                    assert (ase.weight > 0.999999 * estimationFullWeight) : "Inconsistent distance estimate "
                    //                            + ase.weight + " vs " + estimationFullWeight + " (" + ase.weight / estimationFullWeight + "), and:"
                    //                            + ase.getWeightOfVisitedPath() + " vs " + alreadyVisitedWeight + " (" + ase.getWeightOfVisitedPath() / alreadyVisitedWeight + ")";
                    prioQueueOpenSet.remove(ase);
                    ase.edge = iter.getEdge();
                    ase.weight = estimationFullWeight;
                    ase.weightOfVisitedPath = alreadyVisitedWeight;
                }

                ase.parent = currEdge;
                prioQueueOpenSet.add(ase);

                if (updateBestPath)
                    updateBestPathALT(iter, ase, traversalId);
            }
        }
    }
    
    public void updateBestPathALT(EdgeIteratorState edgeState, AStarEntry entryCurrent, int currLoc) {
        AStarEntry entryOther = bestWeightMapOther.get(currLoc);
        if (entryOther == null)
            return;

        boolean reverse = bestWeightMapFrom == bestWeightMapOther;
        // update μ
        double newWeight = entryCurrent.weightOfVisitedPath + entryOther.weightOfVisitedPath;
        if (traversalMode.isEdgeBased()) {
            if (entryOther.edge != entryCurrent.edge)
                throw new IllegalStateException("cannot happen for edge based execution of " + getName());

            // see DijkstraBidirectionRef
            if (entryOther.adjNode != entryCurrent.adjNode) {
                entryCurrent = (AStarEntry) entryCurrent.parent;
                newWeight -= weighting.calcWeight(edgeState, reverse, EdgeIterator.NO_EDGE);
            } else if (!traversalMode.hasUTurnSupport())
                // we detected a u-turn at meeting point, skip if not supported
                return;
        }

        if (newWeight < bestPath.getWeight()) {
            bestPath.setSwitchToFrom(reverse);
            bestPath.setSPTEntry(entryCurrent);
            bestPath.setWeight(newWeight);
            bestPath.setSPTEntryTo(entryOther);
        }
    }

    @Override
    protected void updateBestPath(EdgeIteratorState edgeState, SPTEntry entryCurrent, int traversalId) {
        SPTEntry entryOther = bestWeightMapOther.get(traversalId);
        if (entryOther == null)
            return;

        boolean reverse = bestWeightMapFrom == bestWeightMapOther;

        // update μ
        double newWeight = entryCurrent.weight + entryOther.weight;
        if (traversalMode.isEdgeBased()) {
            if (entryOther.edge != entryCurrent.edge)
                throw new IllegalStateException("cannot happen for edge based execution of " + getName());

            if (entryOther.adjNode != entryCurrent.adjNode) {
                // prevents the path to contain the edge at the meeting point twice and subtract the weight (excluding turn weight => no previous edge)
                entryCurrent = entryCurrent.parent;
                newWeight -= weighting.calcWeight(edgeState, reverse, EdgeIterator.NO_EDGE);
            } else if (!traversalMode.hasUTurnSupport())
                // we detected a u-turn at meeting point, skip if not supported
                return;
        }

        if (newWeight < bestPath.getWeight()) {
            bestPath.setSwitchToFrom(reverse);
            bestPath.setSPTEntry(entryCurrent);
            bestPath.setWeight(newWeight);
            bestPath.setSPTEntryTo(entryOther);
        }
    }

    /**
     * Finds the closest node that is in the core from a node that is not necessarily in the core.
     * Use this node as approximation node in lm approximator in core
     * @param nodeId the nodeId in the basegraph to get the proxynode for
     * @return the proxy node id
     */
    private SPTEntry getProxyNode(int nodeId, boolean bwd) {
        ProxyNodeDijkstra proxyNodeDijkstra = new ProxyNodeDijkstra(graph, weighting, traversalMode);
        SPTEntry proxyNode = proxyNodeDijkstra.getProxyNode(nodeId, bwd);
        visitedCountProxy += proxyNodeDijkstra.getVisitedNodes();
        return proxyNode;
    }

    public int getVisitedNodesProxy() {
        return visitedCountProxy;
    }

    @Override
    public int getVisitedNodes() {
        return super.getVisitedNodes() + getVisitedNodesProxy();
    }

    public static class AStarEntry extends SPTEntry {
        double weightOfVisitedPath;

        public AStarEntry(int edgeId, int adjNode, double weightForHeap, double weightOfVisitedPath) {
            super(edgeId, adjNode, weightForHeap);
            this.weightOfVisitedPath = weightOfVisitedPath;
        }

        @Override
        public final double getWeightOfVisitedPath() {
            return weightOfVisitedPath;
        }
    }

    @Override
    public String getName() {
        return Parameters.Algorithms.ASTAR_BI + "|" + weightApprox;
    }

}
