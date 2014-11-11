<%@ page import="ie.festivals.UserRole" %>

<head>
    <r:require module="highcharts"/>

</head>

<body>

<div class="container main">
    <div id="chart"></div>
</div>

<r:script>
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
</r:script>
</body>
