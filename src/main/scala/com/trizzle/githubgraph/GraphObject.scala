package com.trizzle.githubgraph

/**
  * Created by marktranter on 13/01/2016.
  */

abstract class GraphObject;


case class GraphUserSummary(  name: String,
                              avatar_url: String,
                              html_url: String) extends GraphObject


case class GraphRepository(name: String, description: String, htmlUrl: String, isFork: Boolean,
                           starGazers: Seq[GraphUserSummary], watchers: Seq[GraphUserSummary],
                          forkers: Seq[GraphRepositoryFork],
                           languages: Map[String, BigInt]) extends GraphObject

case class GraphRepositoryFork(name: String)extends GraphObject

object GraphRepositoryFork {

  implicit def repoToGraphRepoFork(repositoryDetail: RepositoryDetail): GraphRepositoryFork ={
    GraphRepositoryFork(repositoryDetail.owner.login)
  }

  implicit def repoToGraphRepoFork(repositoryDetails: Seq[RepositoryDetail]): Seq[GraphRepositoryFork] ={
    if(repositoryDetails != null)
      repositoryDetails map { r =>
        if(r.owner != null)
          GraphRepositoryFork(r.owner.login)
        else
          null
      } filter {
        r => r != null
      }
    else
      null
  }
}

case class GitHubGraphUser(name: String, login: String, githubId: String,
                           avatar_url: String, html_url: String,
                           followers: Seq[GraphUserSummary],
                           repositories: Seq[GraphRepository] ) extends GraphObject

case class GithubGraph(userDetail: GitHubGraphUser) extends GraphObject