%{-- Code for this widget available here https://twitter.com/about/resources/widgets/widget_profile --}%
<a class="twitter-timeline"
   data-screen-name="${username}"
   href="https://twitter.com/${username}"
   width="100%"
   data-widget-id="275025608696795138">Tweets by @${username}</a>

<r:script>!function (d, s, id) {
    var js, fjs = d.getElementsByTagName(s)[0];
    if (!d.getElementById(id)) {
        js = d.createElement(s);
        js.id = id;
        js.src = "//platform.twitter.com/widgets.js";
        fjs.parentNode.insertBefore(js, fjs);
    }
}(document, "script", "twitter-wjs");</r:script>