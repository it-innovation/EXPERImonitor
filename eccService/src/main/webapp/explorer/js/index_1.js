var chart = d3.parsets()
    .dimensions(["QoE", "Activities", "Applications", "Services"]);

var vis = d3.select("#vis").append("svg")
    .attr("width", chart.width())
    .attr("height", chart.height());

var partition = d3.layout.partition()
    .sort(null)
    .size([chart.width(), chart.height() * 5 / 4])
    .children(function(d) { return d.children ? d3.values(d.children) : null; })
    .value(function(d) { return d.count; });

var ice = false;

function curves() {
  var t = vis.transition().duration(500);
  if (ice) {
    t.delay(1000);
    icicle();
  }
  t.call(chart.tension(this.checked ? .5 : 1));
}

d3.csv("json/titanic_1.csv", function(error, data) {
  vis.datum(data).call(chart);
});

function iceTransition(g) {
  return g.transition().duration(1000);
}

function ribbonPath(s, t, tension) {
  var sx = s.node.x0 + s.x0,
      tx = t.node.x0 + t.x0,
      sy = s.dimension.y0,
      ty = t.dimension.y0;
  return (tension === 1 ? [
      "M", [sx, sy],
      "L", [tx, ty],
      "h", t.dx,
      "L", [sx + s.dx, sy],
      "Z"]
   : ["M", [sx, sy],
      "C", [sx, m0 = tension * sy + (1 - tension) * ty], " ",
           [tx, m1 = tension * ty + (1 - tension) * sy], " ", [tx, ty],
      "h", t.dx,
      "C", [tx + t.dx, m1], " ", [sx + s.dx, m0], " ", [sx + s.dx, sy],
      "Z"]).join("");
}

function stopClick() { d3.event.stopPropagation(); }

// Given a text function and width function, truncates the text if necessary to
// fit within the given width.
function truncateText(text, width) {
  return function(d, i) {
    var t = this.textContent = text(d, i),
        w = width(d, i);
    if (this.getComputedTextLength() < w) return t;
    this.textContent = "…" + t;
    var lo = 0,
        hi = t.length + 1,
        x;
    while (lo < hi) {
      var mid = lo + hi >> 1;
      if ((x = this.getSubStringLength(0, mid)) < w) lo = mid + 1;
      else hi = mid;
    }
    return lo > 1 ? t.substr(0, lo - 2) + "…" : "";
  };
}

d3.select("#file").on("change", function() {
  var file = this.files[0],
      reader = new FileReader;
  reader.onloadend = function() {
    var csv = d3.csv.parse(reader.result);
    vis.datum(csv).call(chart
        .value(csv[0].hasOwnProperty("Number") ? function(d) { return +d.Number; } : 1)
        .dimensions(function(d) { return d3.keys(d[0]).filter(function(d) { return d !== "Number"; }).sort(); }));
  };
  reader.readAsText(file);
});
