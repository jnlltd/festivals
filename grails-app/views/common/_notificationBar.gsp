%{--I put this JQuery code in a GSP, so I can use flash.msgBody to display the message--}%

<asset:script>
    (function($) {

        var _options = null;

        $.fn.notificationMsg = function(options) {
            _options = $.extend({}, $.fn.notificationMsg.defaults, options);

            $("#topMessageBar").remove();

            $("body").prepend("<div id='topMessageBar'><div id='msgContainer'><span id='notificationMessage'>"
                    + _options.message + "</span><span id='closeMsg'>[click to close]</span></div></div>");

            $("#topMessageBar").addClass(_options.cssClass);

            if (_options.bFading) {
                $("#topMessageBar").fadeIn();
                $("#messageBarCloseBtn").fadeIn();

            } else {
                $("#topMessageBar").slideDown();
                $("#messageBarCloseBtn").slideDown();
            }

            $("#topMessageBar").click(function(){
                if (_options.bFading) {
                    $("#messageBarCloseBtn").fadeOut();
                    $("#topMessageBar").fadeOut();

                } else {
                    $("#messageBarCloseBtn").hide();
                    $("#topMessageBar").slideUp();
                }
            });
        };

        $.fn.notificationMsg.defaults = { bFading: false };

        <g:if test="${flash.info}">
            $().notificationMsg({message: '${flashMsg.msg(key: "info", remove: "true")}', cssClass: 'MessageBarCommon MessageBarOk'});
        </g:if>

        <g:if test="${flash.warn}">
            $().notificationMsg({message: '${flashMsg.msg(key: "warn", remove: "true")}', cssClass: 'MessageBarCommon MessageBarWarning'});
        </g:if>

    })(jQuery);
</asset:script>