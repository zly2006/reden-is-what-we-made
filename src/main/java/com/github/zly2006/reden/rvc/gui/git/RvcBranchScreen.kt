package com.github.zly2006.reden.rvc.gui.git

import org.eclipse.jgit.revwalk.RevCommit

class RvcBranchScreen {
    class RvcGitTagListComponent {
    }
    class RvcGitTimelineComponent(
        val final: RevCommit
    ) {
        val commits = mutableListOf<RevCommit>()
        init {
            // collect all commits
            val queue = mutableListOf(final)
            while (queue.isNotEmpty()) {
                val commit = queue.removeFirst()
                commits.add(commit)
                commit.parents.forEach {
                    if (it !in commits) {
                        queue.add(it)
                    }
                }
            }
            commits.sortBy { it.commitTime }
            // count max parallel branches
            var branches = 0
            var maxBranches = 0

        }
    }
}
