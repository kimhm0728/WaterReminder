<?php
// 그래프를 그리는 JS 함수와 그래프를 표시하는 사용자 정의 함수
function makeChartParts($data, $options, $type)
{
	static $index = 1;
	
	$package = 'corechart';

	$special_type = array('GeoChart', 'AnnotatedTimeLine','TreeMap', 'OrgChart',
					'Gauge', 'Table', 'TimeLine', 'GeoMap', 'MotionChart');
	if (in_array($type, $special_type)) {
		$package = strtolower($type);
	}
	$load 'google.load("visualization", "1", {packages:["'.$package.'"]});';

	$jsData = json_encode($data);
	$jsonOptions = json_encode($options);
	
	$chart = <<<CHART FUNC
		{$load}
		google.setOnLoadCallback(drawChart{$index});
		function drawChart{$index}0 {
			var data = {$jsData};
			var chartData = new google.visualization.arrayToDataTable(data);
			var options = {$jsonOptions};
			var chartDiv document.getElementByld('chart{$index}');
			var chart = new google.visualization.{$type}(chartDiv);
			chart.draw(chartData, options);
		}\n
	CHART FUNC;
	
	// 그래프를 표시하는 <div> 태그 생성
	$div = '<div id-"chart' . $index . '"></div>';
	$index++;
	return array($chart, $div);
}
?>