<%@ page import="ie.festivals.UserRole" %>

<body>

<div class="container main">
    <div id="chart"></div>
</div>

<asset:script>
    $(function () {
        $('#chart').highcharts({
            chart: {
                type: 'line'
            },
            title: {
                text: 'User Registration Timeline'
            },
            subtitle: {
                text: 'Total: ${total}'
            },
            xAxis: {
                type: 'datetime'
            },
            yAxis: {
                title: {
                    text: 'Confirmed Users'
                },
                min: ${yAxisMin}
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.y}</b><br/>',
                shared: true
            },
            legend: {
                enabled: false
            },
            credits: {
                enabled: false
            },
            series: [{
                name: 'Users',
                data: ${dataSeries}
            }]
        });
    });
</asset:script>

<asset:javascript src="highcharts.min.js"/>
</body>
