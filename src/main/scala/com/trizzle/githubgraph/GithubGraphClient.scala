package com.trizzle.githubgraph

import scala.async.Async._
import dispatch.{url, Req, :/}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by marktranter on 23/12/2015.
  */

class GitHubGraphClient(authToken: String = null)(implicit context: ExecutionContext) {

  private val defaultAccessToken = System.getenv("GITHUB_DEFAULT_ACCESS_TOKEN")

  private val svcRoot = :/("api.github.com").secure

  def getGraph(username: String) : Future[GithubGraph] = async {
    val user = await(getUserDetails(username))
    await(getGraph(user))
  }

  def getGraph(user: GithubUserDetail): Future[GithubGraph] = async {
    val followers =  await(getUserFollowers(user))
    val repos = await(getUserRepos(user))
    val repoDetails = await(Future.sequence(repos))

    GithubGraph(GitHubGraphUser(user.name, user.login,user.id.toString(), user.avatar_url,user.html_url, followers, repoDetails))
  }

  def getUserRepos(user: GithubUserDetail) : Future[Seq[Future[GraphRepository]]] =  async {
    val repos = await(JsonRestClient.get[Seq[RepositoryDetail]](buildRequest(req => url(user.repos_url ))))
    repos.map(r => buildGraphRepo(r))
  }

  def buildGraphRepo(repo: RepositoryDetail): Future[GraphRepository] = async{
    val starGazers: Seq[GraphUserSummary] = if(repo.stargazers_count > 0) await(getRepoStargazers(repo)) else null
    val languages = await(getRepoLanguages(repo))
    GraphRepository(repo.name, repo.description, repo.html_url, repo.fork, starGazers, languages)
  }

  def getRepoStargazers(repo: RepositoryDetail): Future[Seq[GraphUserSummary]] = async {
    val followers = await(JsonRestClient.get[Seq[UserSummary]](buildRequest(r => url(repo.stargazers_url))))
    followers.map(f => GraphUserSummary(f.login, f.avatar_url, f.html_url))
  }

  def getUserFollowers(user: GithubUserDetail): Future[Seq[GraphUserSummary]] = async {
    val followers = await(JsonRestClient.get[Seq[UserSummary]](buildRequest(r => url(user.followers_url))))
    followers.map(f => GraphUserSummary(f.login, f.avatar_url, f.html_url))
  }

  def getRepoLanguages(repo: RepositoryDetail): Future[Map[String, BigInt]] = {
    JsonRestClient.get[Map[String, BigInt]](buildRequest(r => url(repo.languages_url)))
  }

  def getUserDetails(username: String): Future[GithubUserDetail] = {
    JsonRestClient.get[GithubUserDetail](buildRequest(r => r / "users" / username))
  }

  def getCurrentUser(): Future[GithubUserDetail] = {
    JsonRestClient.get[GithubUserDetail](buildRequest(r => r / "user"))
  }

  private def buildRequest(bld: Req => Req): Req = {
    val rt = bld(svcRoot)
    rt <<? Map("access_token" -> defaultAccessToken)
  }
}

case class UserSummary(
                           login: String,
                           id: Double,
                           avatar_url: String,
                           gravatar_id: String,
                           url: String,
                           html_url: String,
                           followers_url: String,
                           following_url: String,
                           gists_url: String,
                           starred_url: String,
                           subscriptions_url: String,
                           organizations_url: String,
                           repos_url: String,
                           events_url: String,
                           received_events_url: String,
                           `type`: String,
                           site_admin: Boolean
                      )

case class Plan(
                 name: String,
                 space: Double,
                 collaborators: Double,
                 private_repos: Double
               )



case class GithubUserDetail(login: String,
                            id: BigInt,
                            avatar_url: String,
                            gravatar_id: String,
                            url: String,
                            html_url: String,
                            followers_url: String,
                            following_url: String,
                            gists_url: String,
                            starred_url: String,
                            subscriptions_url: String,
                            organizations_url: String,
                            repos_url: String,
                            events_url: String,
                            received_events_url: String,
                            `type`: String,
                            site_admin: Boolean,
                            name: String,company: String,
                            blog: String,
                            location: String,
                            email: String,
                            hireable: String,
                            bio: String,
                            public_repos: Double,
                            public_gists: Double,
                            followers: Double,
                            following: Double,
                            created_at: String,
                            updated_at: String,
                            private_gists: Double,
                            total_private_repos: Double,
                            owned_private_repos: Double,
                            disk_usage: Double,
                            collaborators: Double,
                            plan: Plan)


case class Owner(
                  login: String,
                  id: Double,
                  avatar_url: String,
                  gravatar_id: String,
                  url: String,
                  html_url: String,
                  followers_url: String,
                  following_url: String,
                  gists_url: String,
                  starred_url: String,
                  subscriptions_url: String,
                  organizations_url: String,
                  repos_url: String,
                  events_url: String,
                  received_events_url: String,
                  `type`: String,
                  site_admin: Boolean)



case class RepositoryDetail(
                           id: Double,
                           name: String,
                           full_name: String,
                           owner: Owner,
                          `private`: Boolean,
                          html_url: String,
                          description: String,
                          fork: Boolean,
                          url: String,
                          forks_url: String,
                          keys_url: String,
                          collaborators_url: String,
                          teams_url: String,
                          hooks_url: String,
                          issue_events_url: String,
                          events_url: String,
                          assignees_url: String,
                          branches_url: String,
                          tags_url: String,
                          blobs_url: String,
                          git_tags_url: String,
                          git_refs_url: String,
                          trees_url: String,
                          statuses_url: String,
                          languages_url: String,
                          stargazers_url: String,
                          contributors_url: String,
                          subscribers_url: String,
                          subscription_url: String,
                          commits_url: String,
                          git_commits_url: String,
                          comments_url: String,
                          issue_comment_url: String,
                          contents_url: String,
                          compare_url: String,
                          merges_url: String,
                          archive_url: String,
                          downloads_url: String,
                          issues_url: String,
                          pulls_url: String,
                          milestones_url: String,
                          notifications_url: String,
                          labels_url: String,
                          releases_url: String,
                          created_at: String,
                          updated_at: String,
                          pushed_at: String,
                          git_url: String,
                          ssh_url: String,
                          clone_url: String,
                          svn_url: String,
                          homepage: String,
                          size: Double,
                          stargazers_count: Double,
                          watchers_count: Double,
                          language: String,
                          has_issues: Boolean,
                          has_downloads: Boolean,
                          has_wiki: Boolean,
                          has_pages: Boolean,
                          forks_count: Double,
                          mirror_url: String,
                          open_issues_count: Double,
                          forks: Double,
                          open_issues: Double,
                          watchers: Double,
                          default_branch: String,
                          permissions: Permissions,
                          network_count: Double,
                          subscribers_count: Double)


abstract class GraphObject;

case class GraphObjectCollection[T <: GraphObject](children: Seq[T])

 object  GraphObjectCollection{
   implicit def seqToGraphCollection[T <: GraphObject](children: Seq[T]): GraphObjectCollection[T] ={
    GraphObjectCollection(children)
  }
}

case class Permissions(admin: Boolean, push: Boolean, pull: Boolean)


case class GraphUserSummary(  name: String,
                              avatar_url: String,
                              html_url: String) extends GraphObject


case class GraphRepository(name: String, description: String, htmlUrl: String, isFork: Boolean,
                           starGazers: Seq[GraphUserSummary],
                           languages: Map[String, BigInt]) extends GraphObject

case class GitHubGraphUser(name: String, login: String, githubId: String,
                           avatar_url: String, html_url: String,
                          followers: Seq[GraphUserSummary],
                           repositories: Seq[GraphRepository] )

case class GithubGraph(userDetail: GitHubGraphUser)

