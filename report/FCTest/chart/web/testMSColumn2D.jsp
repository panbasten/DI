<HTML>
<HEAD>
	<TITLE>FusionCharts Free & JavaScript - Updating chart using updateChartXML() Method</TITLE>	
	<style type="text/css">
	<!--
	body {
		font-family: Arial, Helvetica, sans-serif;
		font-size: 12px;
	}
	-->
	</style>
	<SCRIPT LANGUAGE="Javascript" SRC="./javascripts/bqCharts.js"></SCRIPT>
	<SCRIPT LANGUAGE="Javascript" SRC="fusioncharts/FusionCharts.js"></SCRIPT>
	<SCRIPT LANGUAGE="Javascript" SRC="chartDefines/MSColumn2D/fusionCharts/MSColumn2D.js"></SCRIPT>
	<script language="javascript">
		var chart = null;
		//结构：[["api1",["string","string","number"]],["api2",["string","object"]]]
		var chartDefine = new ChartDefine("MSColumn2D");
		chartDefine.addAPIParameterTypesMapping("greeting", ["string","string"]);
		chartDefine.addAPIParameterTypesMapping("doAndReturn", ["string","number"]);
		addChartDefine(chartDefine);
		//initAPIParameterMapping('[["greeting",["string","string"]],["doAndReturn",["string","number"]]]');
		function callGreetingAPI(){
			var paras = new Array(2);
			paras[0] = "doit";
			paras[1] = 111;
			var result = chart.callAPIWithResult("doAndReturn", paras);
			alert(result);
		}
		function clearChart(){
			chart.clear();
		}
		
	</script>
</HEAD>
<BODY oncontextmenu="return test()" ondragstart="alert('drag')" onselectstart="return false">
	<CENTER>
		<h2>FusionCharts Free & JavaScript - Updating chart using updateChartXML() method</h2>
		 
		 <div id="chart1div"/>
		 

		<script language="JavaScript">	
		
		function datasetClick(vars){
			alert(vars);
		}
			var data = "<!-- http://docs.fusioncharts.com/charts/contents/ChartSS/MSColumn2D.html -->"
				+ "<chart caption='Business Results 2005 v 2006' xAxisName='Month' yAxisName='Revenue'"
				+ "showValues='0' numberPrefix='$'"
				+ "showVLineLabelBorder='1' >"
				+ "   <categories>"
				+ "      <category label='Jan' link='javascript:alert(1)'/>"
				+ "      <category label='Feb' />"
				+ "      <category label='Mar' />"
				+ "      <category label='Apr' />"
				+ "      <category label='May' />"
				+ "	  <vline color='FF5904' thickness='2' showLabelBorder='1' />"
				+ "      <category label='Jun' />"
				+ "      <category label='Jul' />"
				+ "      <category label='Aug' />"
				+ "      <category label='Sep' />"
				+ "      <category label='Oct' />"
				+ "      <category label='Nov' />"
				+ "      <category label='Dec' />"
				+ "   </categories>"
				+ "   <dataset seriesName='2006'>"
				+ "      <set value='27400'  link='j-datasetClick-asdfasdf,fff' />"
				+ "      <set value='29800'/>"
				+ "      <set value='25800' />"
				+ "      <set value='26800' />"
				+ "     <set value='29600' />"
				+ "      <set value='32600' />"
				+ "      <set value='31800' />"
				+ "      <set value='36700' />"
				+ "      <set value='29700' />"
				+ "      <set value='31900' />"
				+ "      <set value='34800' />"
				+ "      <set value='24800' />"
				+ "   </dataset>"
				+ "   <dataset seriesName='2005'>"
				+ "      <set value='10000'/>"
				+ "      <set value='11500'/>"
				+ "      <set value='12500'/>"
				+ "      <set value='15000'/>"
				+ "     <set value='11000' />"
				+ "      <set value='9800' />"
				+ "      <set value='11800' />"
				+ "      <set value='19700' />"
				+ "      <set value='21700' />"
				+ "      <set value='21900' />"
				+ "      <set value='22900' />"
				+ "      <set value='20800' />"
				+ "   </dataset>"
				+ "   <trendlines>"
				+ "      <line startValue='26000' color='91C728' displayValue='Target' showOnTop='1' toolText='sssss' />"
				+ "   </trendlines>"
				+ "</chart>";
				//(type, chartDefineCode, domId, data, width, height){
				chart = createBQChart("fusioncharts", "MSColumn2D", "chart1div", data, 400, 300);
				//var chart2 = createBQChart(document.getElementById("chart2div"), "fusioncharts", "MSColumn2D", data, 400, 300);
		
				function test(){
					var chartObj = infosoftglobal.FusionChartsUtil.getChartObject(chart.chartId);
					//alert(chartObj);
					chartObj.clickaaa();
					return false;
				}
				
				function hasRendered(){
					alert(chart.hasRendered());
				}
				
				function updateChart(){
					var data2 = "<!-- http://docs.fusioncharts.com/charts/contents/ChartSS/MSColumn2D.html -->"
						+ "<chart caption='Business Results 2005 v 2006' xAxisName='Month' yAxisName='Revenue'"
						+ "showValues='0' numberPrefix='$'"
						+ "showVLineLabelBorder='1' >"
						+ "   <categories>"
						+ "      <category label='Jan' link='javascript:alert(1)'/>"
						+ "      <category label='Feb' />"
						+ "      <category label='Mar' />"
						+ "	  <vline color='FF5904' thickness='2' showLabelBorder='1' />"
						+ "      <category label='Jun' />"
						+ "      <category label='Jul' />"

						+ "   </categories>"
						+ "   <dataset seriesName='2006'>"
						+ "      <set value='27400'  link='j-datasetClick-asdfasdf,fff' />"
						+ "      <set value='29800'/>"
						+ "   </dataset>"
						+ "   <dataset seriesName='2005'>"
						+ "      <set value='10000'/>"
						+ "      <set value='20800' />"
						+ "   </dataset>"
						+ "   <trendlines>"
						+ "      <line startValue='26000' color='91C728' displayValue='Target' showOnTop='1' toolText='sssss' />"
						+ "   </trendlines>"
						+ "</chart>";	
				
						//updateChartXML("chart1div", data);
					chart.update("fusioncharts", "MSColumn2D", data);
				}	
		</script>
		<BR />
		<form name='callGreeting'>
		<input type='button' value='call api' onClick="javaScript:callGreetingAPI();" name='btncallapi' />		
		<input type='button' value='clearChart' onClick="javaScript:clearChart();" name='btnclear' />		
		<input type='button' value='updateChart' onClick="javaScript:updateChart();" name='btnclear' />		
		<input type='button' value="call hasrendered()" onclick="javascript:hasRendered();"/>
		</form>
	</CENTER>
</BODY>
</HTML>
