# Data-Structures-Projects

This repository contains a collection of complex system simulations developed as part of the **CMPE 250: Data Structures and Algorithms** course at Boğaziçi University, where every core data structure (Hash Tables, Heaps, AVL Trees, Graphs) was implemented from scratch to ensure optimal time and space complexity.

## 🚀 Projects Overview

### 📂 Project 1: AVL Tree Based Game Implementation
A card game simulation that manages dynamic entity states using self-balancing trees.
* **Core Concepts:** AVL Trees, rotations, and O(log N) search/update operations.
* **Key Achievement:** Managed game entities efficiently by ensuring the tree remains balanced during frequent insertions and deletions.

### 📂 Project 2: Gig-Economy Platform Simulation (GigMatch Pro)
A freelancer-customer matching platform that handles complex business logic and ranking.
* **Custom Data Structures:** Implemented a **Custom Hash Table** for O(1) lookups and an **Indexed Max-Heap** for priority-based matching.
* **Features:** Loyalty tiers for customers, burnout mechanics for freelancers, and dynamic composite score calculations.

### 📂 Project 3: MatrixNet - Network Simulation with Graph Theory
A robust network simulation tool designed to analyze connectivity and optimal routing within a graph of hosts and backdoors.
* **Graph Algorithms:** Implemented **Dijkstra’s Algorithm** (optimized with a custom Min-Priority Queue) for pathfinding and **BFS/DFS** for connectivity analysis.
* **Key Features:** * Articulation point and bridge detection to simulate network breaches.
    * Dynamic latency calculation based on network congestion factors ($\lambda$).
    * Cycle detection using stack-based DFS to prevent overhead.

---

## 🛠 Tech Stack & Skills
* **Language:** Java
* **Structures Built from Scratch:** Hash Table, Max-Heap, Min-Heap, AVL Tree, Graph Adjacency List.
* **Problem Solving:** Algorithmic efficiency (Big O analysis), memory management, and system design.

## 📈 Performance Focus
All projects emphasize performance. For instance, in Project 3, the routing algorithm uses **dominance pruning** to significantly reduce the search space in multi-objective pathfinding scenarios.
