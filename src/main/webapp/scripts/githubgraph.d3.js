
    /**
     * Created by marktranter on 09/01/2016.
     */

    (function(d3){
        var width = 1000,
            height = 1100;
        var diameter = 750;
        var duration = 1000;
        var linkPaths, nodeGroups, circles;
        var root;
        var TEXT_MAP = {"followers": "Followers", "starGazers": "Star Gazers"};
        var FORCE_CFG_MAP = {
            "Mark Tranter": {charge: -500, linkDistance: 400},
            "starGazers": {charge: -100, linkDistance: 50} ,
            "repositories": {charge: -200, linkDistance: 200}
        }

        d3.selectAll("input").on("change", change);

        function change() {
            if (this.value === "radialtree") {
                force.stop();
                transitionToRadialTree();
            }
            else if (this.value === "radialcluster") {
                force.stop();
                transitionToRadialCluster();
            }
            else if (this.value === "cluster") {
                force.stop();
                transitionToCluster();
            }
            else if (this.value === "tree") {
                force.stop();
                transitionToTree();
            }
            else
                transitionToForce();
        };

        function transitionToRadialTree() {

            var nodes = radialTree.nodes(root),
                links = radialTree.links(nodes);

            svg.transition().duration(duration)
                .attr("transform", "translate(" + (width/2) + "," +
                    (height/2) + ")");

            linkPaths.data(links)
                .transition()
                .duration(duration)
                .style("stroke", "#fc8d62")
                .attr("d", radialDiagonal);

            nodeGroups.data(nodes)
                .transition()
                .duration(duration)
                .attr("transform", function(d) {
                    return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
                });

            nodeGroups.select("circle")
                .transition()
                .duration(duration)
                .style("stroke", "#984ea3");

        };

        function transitionToRadialCluster() {

            var nodes = radialCluster.nodes(root),
                links = radialCluster.links(nodes);

            svg.transition().duration(duration)
                .attr("transform", "translate(" + (width/2) + "," +
                    (height/2) + ")");

            linkPaths.data(links)
                .transition()
                .duration(duration)
                .style("stroke", "#66c2a5")
                .attr("d", radialDiagonal);

            nodeGroups.data(nodes)
                .transition()
                .duration(duration)
                .attr("transform", function(d) {
                    return "rotate(" + (d.x - 90) + ")translate(" + d.y + ")";
                });

            nodeGroups.select("circle")
                .transition()
                .duration(duration)
                .style("stroke", "#4daf4a");

        };

        function transitionToTree() {

            var nodes = tree.nodes(root), //recalculate layout
                links = tree.links(nodes);

            svg.transition().duration(duration)
                .attr("transform", "translate(40,0)");

            linkPaths.data(links)
                .transition()
                .duration(duration)
                .style("stroke", "#e78ac3")
                .attr("d", diagonal); // get the new tree path

            nodeGroups.data(nodes)
                .transition()
                .duration(duration)
                .attr("transform", function (d) {
                    return "translate(" + d.y + "," + d.x + ")";
                });

            nodeGroups.select("circle")
                .transition()
                .duration(duration)
                .style("stroke", "#377eb8");

        };

        function transitionToCluster() {

            var nodes = cluster.nodes(root), //recalculate layout
                links = cluster.links(nodes);

            svg.transition().duration(duration)
                .attr("transform", "translate(40,0)");

            linkPaths.data(links)
                .transition()
                .duration(duration)
                .style("stroke", "#8da0cb")
                .attr("d", diagonal); //get the new cluster path

            nodeGroups.data(nodes)
                .transition()
                .duration(duration)
                .attr("transform", function (d) {
                    return "translate(" + d.y + "," + d.x + ")";
                });

            nodeGroups.select("circle")
                .transition()
                .duration(duration)
                .style("stroke", "#e41a1c");

        };

        function transitionToForce() {
            circles.transition()
                .attr("r", 10)
                .duration(duration);

            force.start();
        }


        function calcForceParam(d, facet){

            var params = FORCE_CFG_MAP[d];
            if(params){
                console.log(params[facet])
                return params[facet]
            }

            return (50 * (facet == 'charge' ? -1 : 1));
        }


        var force = d3.layout.force()
            .charge(function(d){ return d.name ? calcForceParam(d.name, 'charge') : -100})
            .linkDistance(function(d){ return d.name ? calcForceParam(d.name, 'linkDistance') : 100})
            .linkStrength(10)
            .chargeDistance(1000)
            .gravity(.01)
            .theta(0.2)
            .size([width, height]);

        var tree = d3.layout.tree()
            .size([height, width - 160]);

        var cluster = d3.layout.cluster()
            .size([height, width - 160]);

        var diagonal = d3.svg.diagonal()
            .projection(function (d) {
                return [d.y, d.x];
            });

        var radialTree = d3.layout.tree()
            .size([360, diameter / 2 ])
            .separation(function(a, b) {
                return (a.parent == b.parent ? 1 : 2) / a.depth;
            });

        var radialCluster = d3.layout.cluster()
            .size([360, diameter / 2 ])
            .separation(function(a, b) {
                return (a.parent == b.parent ? 1 : 2) / a.depth;
            });

        var radialDiagonal = d3.svg.diagonal.radial()
            .projection(function(d) {
                return [d.y, d.x / 180 * Math.PI];
            });


        var svg = d3.select("body").append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g")
            .attr("transform", "translate(40,0)");

        function nodeify(source){
            if( (typeof source !== "object") || (source === null) ){
                return source;
            }
            var retval = {};
            for(var p in source){
                if(Array.isArray(source[p])){
                    if(source[p].length > 0){
                        retval.children = retval.children || [];
                        var child = {};
                        child.name = p;
                        child.children = []
                        source[p].forEach(function(v){
                            child.children.push(nodeify(v));
                        });
                        retval.children.push(child);
                    }
                }else{
                    retval[p] = nodeify(source[p]);
                }
            }
            return retval;
        }

        d3.json('http://api.githubgraph.io:8080/user/mtranter', function(err, data){

            root = nodeify(data.userDetail);
            //root.name='userDetail';

            var nodes = cluster.nodes(root),
                links = cluster.links(nodes);

            linkPaths = svg.selectAll(".link")
                .data(links)
                .enter()
                .append("path")
                .attr("class", "link")
                .style("stroke", "#8da0cb")
                .attr("d", diagonal);

            nodeGroups = svg.selectAll(".node")
                .data(nodes)
                .enter()
                .append("g")
                .attr("class", "node")
                .attr("transform", function (d) {
                    return "translate(" + d.y + "," + d.x + ")";
                });

            nodeGroups.append("text")
                .attr("dx", function(d) { return d.children ? 0 : 10; })
                .attr("dy", function(d) { return d.children ? -10 : 3; })
                .attr("text-anchor", function(d) { return d.children ? "middle" : "start"; })
                .text(function(d) { return TEXT_MAP[d.name] || d.name; });

            circles = nodeGroups.append("circle")
                .attr("r", 4.5)
                .style("stroke", "#e41a1c");

            force
                .nodes(nodes)                 // reuse tree.nodes()
                .links(links)					        // reuse tree.links()
                .on("tick", function(e) {		  // update force layout
                    nodeGroups.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })
                    linkPaths.attr("d", diagonal);
                });
        });

    })(d3);