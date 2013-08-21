/**
 * 统计图类
 * 
 * @param {Object} options
 * @param {Function} callback 统计图加载完成后执行的方法
 */
$FC.Chart = function () {
	this.init.apply(this, arguments);
};

$FC.Chart.prototype = {

	/**
	 * 初始化图表
	 */
	init: function (userOptions, callback) {

		// Handle regular options
		var options,
			seriesOptions = userOptions.series; // 忽略合并数据点，以提高性能

		userOptions.series = null;
		options = $FC.merge($FC.defaultOptions, userOptions); // do the merge
		options.series = userOptions.series = seriesOptions; // set back the series data

		var optionsChart = options.chart,
			optionsMargin = optionsChart.margin,
			margin = $FC.isObject(optionsMargin) ?
				optionsMargin :
				[optionsMargin, optionsMargin, optionsMargin, optionsMargin];

		this.optionsMarginTop = $FC.pick(optionsChart.marginTop, margin[0]);
		this.optionsMarginRight = $FC.pick(optionsChart.marginRight, margin[1]);
		this.optionsMarginBottom = $FC.pick(optionsChart.marginBottom, margin[2]);
		this.optionsMarginLeft = $FC.pick(optionsChart.marginLeft, margin[3]);

		var chartEvents = optionsChart.events;

		//this.runChartClick = chartEvents && !!chartEvents.click;
		this.bounds = { h: {}, v: {} }; // Pixel data bounds for touch zoom

		this.callback = callback;
		this.isResizing = 0;
		this.options = options;
		//chartTitleOptions = UNDEFINED;
		//chartSubtitleOptions = UNDEFINED;

		this.axes = [];
		this.series = [];
		this.hasCartesianSeries = optionsChart.showAxes;
		//this.axisOffset = UNDEFINED;
		//this.maxTicks = UNDEFINED; // handle the greatest amount of ticks on grouped axes
		//this.inverted = UNDEFINED;
		//this.loadingShown = UNDEFINED;
		//this.container = UNDEFINED;
		//this.chartWidth = UNDEFINED;
		//this.chartHeight = UNDEFINED;
		//this.marginRight = UNDEFINED;
		//this.marginBottom = UNDEFINED;
		//this.containerWidth = UNDEFINED;
		//this.containerHeight = UNDEFINED;
		//this.oldChartWidth = UNDEFINED;
		//this.oldChartHeight = UNDEFINED;

		//this.renderTo = UNDEFINED;
		//this.renderToClone = UNDEFINED;

		//this.spacingBox = UNDEFINED

		//this.legend = UNDEFINED;

		// Elements
		//this.chartBackground = UNDEFINED;
		//this.plotBackground = UNDEFINED;
		//this.plotBGImage = UNDEFINED;
		//this.plotBorder = UNDEFINED;
		//this.loadingDiv = UNDEFINED;
		//this.loadingSpan = UNDEFINED;

		var chart = this,
			eventType;

		// 添加统计图到一个全局集合中，便于查找
		chart.index = $FC.charts.length;
		$FC.charts.push(chart);

		// 设置自动调整尺寸
		if (optionsChart.reflow !== false) {
			$FC.addEvent(chart, 'load', function () {
				chart.initReflow();
			});
		}

		// 事件句柄
		if (chartEvents) {
			for (eventType in chartEvents) {
				$FC.addEvent(chart, eventType, chartEvents[eventType]);
			}
		}

		chart.xAxis = [];
		chart.yAxis = [];

		// 暴露的方法和变量
		chart.animation = $FC.useCanVG ? false : $FC.pick(optionsChart.animation, true);
		chart.pointCount = 0;
		chart.counters = new $FC.ChartCounters();

		chart.firstRender();
	},

	/**
	 * Initialize an individual series, called internally before render time
	 */
	initSeries: function (options) {
		var chart = this,
			optionsChart = chart.options.chart,
			type = options.type || optionsChart.type || optionsChart.defaultSeriesType,
			series,
			constr = $FC.seriesTypes[type];

		// No such series type
		if (!constr) {
			error(17, true);
		}

		series = new constr();
		series.init(this, options);
		return series;
	},

	/**
	 * Add a series dynamically after  time
	 *
	 * @param {Object} options The config options
	 * @param {Boolean} redraw Whether to redraw the chart after adding. Defaults to true.
	 * @param {Boolean|Object} animation Whether to apply animation, and optionally animation
	 *    configuration
	 *
	 * @return {Object} series The newly created series object
	 */
	addSeries: function (options, redraw, animation) {
		var series,
			chart = this;

		if (options) {
			redraw = $FC.pick(redraw, true); // defaults to true

			$FC.fireEvent(chart, 'addSeries', { options: options }, function () {
				series = chart.initSeries(options);
				
				chart.isDirtyLegend = true; // the series array is out of sync with the display
				if (redraw) {
					chart.redraw(animation);
				}
			});
		}

		return series;
	},

	/**
     * Add an axis to the chart
     * @param {Object} options The axis option
     * @param {Boolean} isX Whether it is an X axis or a value axis
     */
	addAxis: function (options, isX, redraw, animation) {
		var key = isX ? 'xAxis' : 'yAxis',
			chartOptions = this.options,
			axis;

		/*jslint unused: false*/
		axis = new Axis(this, $FC.merge(options, {
			index: this[key].length,
			isX: isX
		}));
		/*jslint unused: true*/

		// Push the new axis options to the chart options
		chartOptions[key] = splat(chartOptions[key] || {});
		chartOptions[key].push(options);

		if ($FC.pick(redraw, true)) {
			this.redraw(animation);
		}
	},

	/**
	 * Check whether a given point is within the plot area
	 *
	 * @param {Number} plotX Pixel x relative to the plot area
	 * @param {Number} plotY Pixel y relative to the plot area
	 * @param {Boolean} inverted Whether the chart is inverted
	 */
	isInsidePlot: function (plotX, plotY, inverted) {
		var x = inverted ? plotY : plotX,
			y = inverted ? plotX : plotY;
			
		return x >= 0 &&
			x <= this.plotWidth &&
			y >= 0 &&
			y <= this.plotHeight;
	},

	/**
	 * Adjust all axes tick amounts
	 */
	adjustTickAmounts: function () {
		if (this.options.chart.alignTicks !== false) {
			$FC.each(this.axes, function (axis) {
				axis.adjustTickAmount();
			});
		}
		this.maxTicks = null;
	},

	/**
	 * Redraw legend, axes or series based on updated data
	 *
	 * @param {Boolean|Object} animation Whether to apply animation, and optionally animation
	 *    configuration
	 */
	redraw: function (animation) {
		var chart = this,
			axes = chart.axes,
			series = chart.series,
			pointer = chart.pointer,
			legend = chart.legend,
			redrawLegend = chart.isDirtyLegend,
			hasStackedSeries,
			hasDirtyStacks,
			isDirtyBox = chart.isDirtyBox, // todo: check if it has actually changed?
			seriesLength = series.length,
			i = seriesLength,
			serie,
			renderer = chart.renderer,
			isHiddenChart = renderer.isHidden(),
			afterRedraw = [];
			
		setAnimation(animation, chart);
		
		if (isHiddenChart) {
			chart.cloneRenderTo();
		}

		// link stacked series
		while (i--) {
			serie = series[i];

			if (serie.options.stacking) {
				hasStackedSeries = true;
				
				if (serie.isDirty) {
					hasDirtyStacks = true;
					break;
				}
			}
		}
		if (hasDirtyStacks) { // mark others as dirty
			i = seriesLength;
			while (i--) {
				serie = series[i];
				if (serie.options.stacking) {
					serie.isDirty = true;
				}
			}
		}

		// handle updated data in the series
		$FC.each(series, function (serie) {
			if (serie.isDirty) { // prepare the data so axis can read it
				if (serie.options.legendType === 'point') {
					redrawLegend = true;
				}
			}
		});

		// handle added or removed series
		if (redrawLegend && legend.options.enabled) { // series or pie points are added or removed
			// draw legend graphics
			legend.render();

			chart.isDirtyLegend = false;
		}

		// reset stacks
		if (hasStackedSeries) {
			chart.getStacks();
		}


		if (chart.hasCartesianSeries) {
			if (!chart.isResizing) {

				// reset maxTicks
				chart.maxTicks = null;

				// set axes scales
				$FC.each(axes, function (axis) {
					axis.setScale();
				});
			} else {
				// build stacks
				$FC.each(axes, function (axis) {
					axis.buildStacks();
				});
			}
			chart.adjustTickAmounts();
			chart.getMargins();

			// redraw axes
			$FC.each(axes, function (axis) {
				
				// Fire 'afterSetExtremes' only if extremes are set
				if (axis.isDirtyExtremes) { // #821
					axis.isDirtyExtremes = false;
					afterRedraw.push(function () { // prevent a recursive call to chart.redraw() (#1119)
						$FC.fireEvent(axis, 'afterSetExtremes', axis.getExtremes()); // #747, #751
					});
				}
								
				if (axis.isDirty || isDirtyBox || hasStackedSeries) {
					axis.redraw();
					isDirtyBox = true; // #792
				}
			});


		}
		// the plot areas size has changed
		if (isDirtyBox) {
			chart.drawChartBox();
		}


		// redraw affected series
		$FC.each(series, function (serie) {
			if (serie.isDirty && serie.visible &&
					(!serie.isCartesian || serie.xAxis)) { // issue #153
				serie.redraw();
			}
		});

		// move tooltip or reset
		if (pointer && pointer.reset) {
			pointer.reset(true);
		}

		// redraw if canvas
		renderer.draw();

		// fire the event
		$FC.fireEvent(chart, 'redraw'); // jQuery breaks this when calling it from addEvent. Overwrites chart.redraw
		
		if (isHiddenChart) {
			chart.cloneRenderTo(true);
		}
		
		// Fire callbacks that are put on hold until after the redraw
		$FC.each(afterRedraw, function (callback) {
			callback.call();
		});
	},



	/**
	 * Dim the chart and show a loading text or symbol
	 * @param {String} str An optional text to show in the loading label instead of the default one
	 */
	showLoading: function (str) {
		var chart = this,
			options = chart.options,
			loadingDiv = chart.loadingDiv;

		var loadingOptions = options.loading;

		// create the layer at the first call
		if (!loadingDiv) {
			chart.loadingDiv = loadingDiv = $FC.createElement($FC.DIV, {
				className: $FC.PREFIX + 'loading'
			}, $FC.extend(loadingOptions.style, {
				zIndex: 10,
				display: $FC.NONE
			}), chart.container);

			chart.loadingSpan = $FC.createElement(
				'span',
				null,
				loadingOptions.labelStyle,
				loadingDiv
			);

		}

		// update text
		chart.loadingSpan.innerHTML = str || options.lang.loading;

		// show it
		if (!chart.loadingShown) {
			$FC.css(loadingDiv, { 
				opacity: 0, 
				display: '',
				left: chart.plotLeft + $FC.PX,
				top: chart.plotTop + $FC.PX,
				width: chart.plotWidth + $FC.PX,
				height: chart.plotHeight + $FC.PX
			});
			animate(loadingDiv, {
				opacity: loadingOptions.style.opacity
			}, {
				duration: loadingOptions.showDuration || 0
			});
			chart.loadingShown = true;
		}
	},

	/**
	 * Hide the loading layer
	 */
	hideLoading: function () {
		var options = this.options,
			loadingDiv = this.loadingDiv;

		if (loadingDiv) {
			animate(loadingDiv, {
				opacity: 0
			}, {
				duration: options.loading.hideDuration || 100,
				complete: function () {
					$FC.css(loadingDiv, { display: $FC.NONE });
				}
			});
		}
		this.loadingShown = false;
	},

	/**
	 * Get an axis, series or point object by id.
	 * @param id {String} The id as given in the configuration options
	 */
	get: function (id) {
		var chart = this,
			axes = chart.axes,
			series = chart.series;

		var i,
			j,
			points;

		// search axes
		for (i = 0; i < axes.length; i++) {
			if (axes[i].options.id === id) {
				return axes[i];
			}
		}

		// search series
		for (i = 0; i < series.length; i++) {
			if (series[i].options.id === id) {
				return series[i];
			}
		}

		// search points
		for (i = 0; i < series.length; i++) {
			points = series[i].points || [];
			for (j = 0; j < points.length; j++) {
				if (points[j].id === id) {
					return points[j];
				}
			}
		}
		return null;
	},

	/**
	 * Create the Axis instances based on the config options
	 */
	getAxes: function () {
		var chart = this,
			options = this.options,
			xAxisOptions = options.xAxis = $FC.splat(options.xAxis || {}),
			yAxisOptions = options.yAxis = $FC.splat(options.yAxis || {}),
			optionsArray,
			axis;

		// make sure the options are arrays and add some members
		$FC.each(xAxisOptions, function (axis, i) {
			axis.index = i;
			axis.isX = true;
		});

		$FC.each(yAxisOptions, function (axis, i) {
			axis.index = i;
		});

		// concatenate all axis options into one array
		optionsArray = xAxisOptions.concat(yAxisOptions);

		$FC.each(optionsArray, function (axisOptions) {
			axis = new $FC.Axis(chart, axisOptions);
		});

		chart.adjustTickAmounts();
	},


	/**
	 * Get the currently selected points from all series
	 */
	getSelectedPoints: function () {
		var points = [];
		$FC.each(this.series, function (serie) {
			points = points.concat(grep(serie.points || [], function (point) {
				return point.selected;
			}));
		});
		return points;
	},

	/**
	 * Get the currently selected series
	 */
	getSelectedSeries: function () {
		return grep(this.series, function (serie) {
			return serie.selected;
		});
	},

	/**
	 * Generate stacks for each series and calculate stacks total values
	 */
	getStacks: function () {
		var chart = this;

		// reset stacks for each yAxis
		$FC.each(chart.yAxis, function (axis) {
			if (axis.stacks && axis.hasVisibleSeries) {
				axis.oldStacks = axis.stacks;
			}
		});

		$FC.each(chart.series, function (series) {
			if (series.options.stacking && (series.visible === true || chart.options.chart.ignoreHiddenSeries === false)) {
				series.stackKey = series.type + $FC.pick(series.options.stack, '');
			}
		});
	},

	/**
	 * Display the zoom button
	 */
	showResetZoom: function () {
		var chart = this,
			lang = $FC.defaultOptions.lang,
			btnOptions = chart.options.chart.resetZoomButton,
			theme = btnOptions.theme,
			states = theme.states,
			alignTo = btnOptions.relativeTo === 'chart' ? null : 'plotBox';
			
		this.resetZoomButton = chart.renderer.button(lang.resetZoom, null, null, function () { chart.zoomOut(); }, theme, states && states.hover)
			.attr({
				align: btnOptions.position.align,
				title: lang.resetZoomTitle
			})
			.add()
			.align(btnOptions.position, false, alignTo);
			
	},

	/**
	 * Zoom out to 1:1
	 */
	zoomOut: function () {
		var chart = this;
		$FC.fireEvent(chart, 'selection', { resetSelection: true }, function () { 
			chart.zoom();
		});
	},

	/**
	 * Zoom into a given portion of the chart given by axis coordinates
	 * @param {Object} event
	 */
	zoom: function (event) {
		var chart = this,
			hasZoomed,
			pointer = chart.pointer,
			displayButton = false,
			resetZoomButton;

		// If zoom is called with no arguments, reset the axes
		if (!event || event.resetSelection) {
			$FC.each(chart.axes, function (axis) {
				hasZoomed = axis.zoom();
			});
		} else { // else, zoom in on all axes
			$FC.each(event.xAxis.concat(event.yAxis), function (axisData) {
				var axis = axisData.axis,
					isXAxis = axis.isXAxis;

				// don't zoom more than minRange
				if (pointer[isXAxis ? 'zoomX' : 'zoomY'] || pointer[isXAxis ? 'pinchX' : 'pinchY']) {
					hasZoomed = axis.zoom(axisData.min, axisData.max);
					if (axis.displayBtn) {
						displayButton = true;
					}
				}
			});
		}
		
		// Show or hide the Reset zoom button
		resetZoomButton = chart.resetZoomButton;
		if (displayButton && !resetZoomButton) {
			chart.showResetZoom();
		} else if (!displayButton && isObject(resetZoomButton)) {
			chart.resetZoomButton = resetZoomButton.destroy();
		}
		

		// Redraw
		if (hasZoomed) {
			chart.redraw(
				$FC.pick(chart.options.chart.animation, event && event.animation, chart.pointCount < 100) // animation
			);
		}
	},

	/**
	 * Pan the chart by dragging the mouse across the pane. This function is called
	 * on mouse move, and the distance to pan is computed from chartX compared to
	 * the first chartX position in the dragging operation.
	 */
	pan: function (chartX) {
		var chart = this,
			xAxis = chart.xAxis[0],
			mouseDownX = chart.mouseDownX,
			halfPointRange = xAxis.pointRange / 2,
			extremes = xAxis.getExtremes(),
			newMin = xAxis.translate(mouseDownX - chartX, true) + halfPointRange,
			newMax = xAxis.translate(mouseDownX + chart.plotWidth - chartX, true) - halfPointRange,
			hoverPoints = chart.hoverPoints;

		// remove active points for shared tooltip
		if (hoverPoints) {
			$FC.each(hoverPoints, function (point) {
				point.setState();
			});
		}

		if (xAxis.series.length && newMin > $FC.mathMin(extremes.dataMin, extremes.min) && newMax < $FC.mathMax(extremes.dataMax, extremes.max)) {
			xAxis.setExtremes(newMin, newMax, true, false, { trigger: 'pan' });
		}

		chart.mouseDownX = chartX; // set new reference for next run
		$FC.css(chart.container, { cursor: 'move' });
	},

	/**
	 * Show the title and subtitle of the chart
	 *
	 * @param titleOptions {Object} New title options
	 * @param subtitleOptions {Object} New subtitle options
	 *
	 */
	setTitle: function (titleOptions, subtitleOptions) {
		var chart = this,
			options = chart.options,
			chartTitleOptions,
			chartSubtitleOptions;

		chartTitleOptions = options.title = $FC.merge(options.title, titleOptions);
		chartSubtitleOptions = options.subtitle = $FC.merge(options.subtitle, subtitleOptions);

		// add title and subtitle
		$FC.each([
			['title', titleOptions, chartTitleOptions],
			['subtitle', subtitleOptions, chartSubtitleOptions]
		], function (arr) {
			var name = arr[0],
				title = chart[name],
				titleOptions = arr[1],
				chartTitleOptions = arr[2];

			if (title && titleOptions) {
				chart[name] = title = title.destroy(); // remove old
			}
			
			if (chartTitleOptions && chartTitleOptions.text && !title) {
				chart[name] = chart.renderer.text(
					chartTitleOptions.text,
					0,
					0,
					chartTitleOptions.useHTML
				)
				.attr({
					align: chartTitleOptions.align,
					'class': $FC.PREFIX + name,
					zIndex: chartTitleOptions.zIndex || 4
				})
				.css(chartTitleOptions.style)
				.add()
				.align(chartTitleOptions, false, 'spacingBox');
			}
		});

	},

	/**
	 * 根据设置选项和容器尺寸，获得统计图的长宽尺寸
	 */
	getChartSize: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			renderTo = chart.renderToClone || chart.renderTo;

		// 获得内部长宽 (#824)
		chart.containerWidth = $FC.adapterRun(renderTo, 'width');
		chart.containerHeight = $FC.adapterRun(renderTo, 'height');
		
		chart.chartWidth = $FC.mathMax(0, optionsChart.width || chart.containerWidth || 600); // #1393, 1460
		chart.chartHeight = $FC.mathMax(0, $FC.pick(optionsChart.height,
			// 对于一个空容器，一般标准的浏览器的offsetHeight是0，但是IE7是19，所以默认大于19为非空
			chart.containerHeight > 19 ? chart.containerHeight : 400));
	},

	/**
	 * 克隆一个容器DIV（renderTo），放置在外部可视窗口，用于计算其尺寸（hart.render and chart.redraw）
	 */
	cloneRenderTo: function (revert) {
		var clone = this.renderToClone,
			container = this.container;
		
		// 销毁克隆容器，并将容器复制回真实的(renderTo)DIV
		if (revert) {
			if (clone) {
				this.renderTo.appendChild(container);
				discardElement(clone);
				delete this.renderToClone;
			}
		
		// 克隆容器
		} else {
			if (container && container.parentNode === this.renderTo) {
				this.renderTo.removeChild(container); // do not clone this
			}
			this.renderToClone = clone = this.renderTo.cloneNode(0);
			$FC.css(clone, {
				position: $FC.ABSOLUTE,
				top: '-9999px',
				display: 'block' // #833
			});
			doc.body.appendChild(clone);
			if (container) {
				clone.appendChild(container);
			}
		}
	},

	/**
	 * 获得容器对象，探测其尺寸，并创建一个内部DIV容器用于显示统计图
	 */
	getContainer: function () {
		var chart = this,
			container,
			optionsChart = chart.options.chart,
			chartWidth,
			chartHeight,
			renderTo,
			indexAttrName = 'data-highcharts-chart',
			oldChartIndex,
			containerId;

		chart.renderTo = renderTo = optionsChart.renderTo;
		containerId = $FC.PREFIX + $FC.idCounter++;

		if ($FC.isString(renderTo)) {
			chart.renderTo = renderTo = doc.getElementById(renderTo);
		}
		
		// 如果没有设置renderTO属性，提示一个错误
		if (!renderTo) {
			$FC.error(13, true);
		}
		
		// 如果容器中已经加载了一个统计图，将其销毁（通过'data-highcharts-chart'属性获得该统计图）
		oldChartIndex = $FC.pInt($FC.attr(renderTo, indexAttrName));
		if (!isNaN(oldChartIndex) && $FC.charts[oldChartIndex]) {
			$FC.charts[oldChartIndex].destroy();
		}		
		
		// 在容器DIV上标记一个引用（'data-highcharts-chart'属性）
		$FC.attr(renderTo, indexAttrName, chart.index);

		// 移出之前的统计图
		renderTo.innerHTML = '';

		// 如果容器获得不到offsetWidth，说明其不可显示或者是一个不可显示节点的子节点(display:none)。
		// 我们需要暂时将其转到一个可见状态，来探测其尺寸，否则图例和提示都将无法正确渲染。
		if (!renderTo.offsetWidth) {
			chart.cloneRenderTo();
		}

		// 获得宽度和高度
		chart.getChartSize();
		chartWidth = chart.chartWidth;
		chartHeight = chart.chartHeight;

		// 创建一个内部容器
		chart.container = container = $FC.createElement($FC.DIV, {
				className: $FC.PREFIX + 'container' +
					(optionsChart.className ? ' ' + optionsChart.className : ''),
				id: containerId
			}, $FC.extend({
				position: $FC.RELATIVE,
				overflow: $FC.HIDDEN, // needed for context menu (avoid scrollbars) and
					// content overflow in IE
				width: chartWidth + $FC.PX,
				height: chartHeight + $FC.PX,
				textAlign: 'left',
				lineHeight: 'normal', // #427
				zIndex: 0, // #1072
				'-webkit-tap-highlight-color': 'rgba(0,0,0,0)'
			}, optionsChart.style),
			chart.renderToClone || renderTo
		);

		// 缓存指针 (#1650)
		chart._cursor = container.style.cursor;

		chart.renderer =
			optionsChart.forExport ? // 用于导出，强制使用SVG渲染器
				new $FC.SVGRenderer(container, chartWidth, chartHeight, true) :
				new $FC.Renderer(container, chartWidth, chartHeight);

		if ($FC.useCanVG) {
			// 如果使用canvg库，需要扩展和配置渲染器，用于跟踪鼠标事件
			chart.renderer.create(chart, container, chartWidth, chartHeight);
		}
	},

	/**
	 * Calculate margins by rendering axis labels in a preliminary position. Title,
	 * subtitle and legend have already been rendered at this stage, but will be
	 * moved into their final positions
	 */
	getMargins: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			spacingTop = optionsChart.spacingTop,
			spacingRight = optionsChart.spacingRight,
			spacingBottom = optionsChart.spacingBottom,
			spacingLeft = optionsChart.spacingLeft,
			axisOffset,
			legend = chart.legend,
			optionsMarginTop = chart.optionsMarginTop,
			optionsMarginLeft = chart.optionsMarginLeft,
			optionsMarginRight = chart.optionsMarginRight,
			optionsMarginBottom = chart.optionsMarginBottom,
			chartTitleOptions = chart.options.title,
			chartSubtitleOptions = chart.options.subtitle,
			legendOptions = chart.options.legend,
			legendMargin = $FC.pick(legendOptions.margin, 10),
			legendX = legendOptions.x,
			legendY = legendOptions.y,
			align = legendOptions.align,
			verticalAlign = legendOptions.verticalAlign,
			titleOffset;

		chart.resetMargins();
		axisOffset = chart.axisOffset;

		// adjust for title and subtitle
		if ((chart.title || chart.subtitle) && !$FC.defined(chart.optionsMarginTop)) {
			titleOffset = $FC.mathMax(
				(chart.title && !chartTitleOptions.floating && !chartTitleOptions.verticalAlign && chartTitleOptions.y) || 0,
				(chart.subtitle && !chartSubtitleOptions.floating && !chartSubtitleOptions.verticalAlign && chartSubtitleOptions.y) || 0
			);
			if (titleOffset) {
				chart.plotTop = $FC.mathMax(chart.plotTop, titleOffset + $FC.pick(chartTitleOptions.margin, 15) + spacingTop);
			}
		}
		// adjust for legend
		if (legend.display && !legendOptions.floating) {
			if (align === 'right') { // horizontal alignment handled first
				if (!$FC.defined(optionsMarginRight)) {
					chart.marginRight = $FC.mathMax(
						chart.marginRight,
						legend.legendWidth - legendX + legendMargin + spacingRight
					);
				}
			} else if (align === 'left') {
				if (!$FC.defined(optionsMarginLeft)) {
					chart.plotLeft = $FC.mathMax(
						chart.plotLeft,
						legend.legendWidth + legendX + legendMargin + spacingLeft
					);
				}

			} else if (verticalAlign === 'top') {
				if (!$FC.defined(optionsMarginTop)) {
					chart.plotTop = $FC.mathMax(
						chart.plotTop,
						legend.legendHeight + legendY + legendMargin + spacingTop
					);
				}

			} else if (verticalAlign === 'bottom') {
				if (!$FC.defined(optionsMarginBottom)) {
					chart.marginBottom = $FC.mathMax(
						chart.marginBottom,
						legend.legendHeight - legendY + legendMargin + spacingBottom
					);
				}
			}
		}

		// adjust for scroller
		if (chart.extraBottomMargin) {
			chart.marginBottom += chart.extraBottomMargin;
		}
		if (chart.extraTopMargin) {
			chart.plotTop += chart.extraTopMargin;
		}

		// pre-render axes to get labels offset width
		if (chart.hasCartesianSeries) {
			$FC.each(chart.axes, function (axis) {
				axis.getOffset();
			});
		}
		
		if (!$FC.defined(optionsMarginLeft)) {
			chart.plotLeft += axisOffset[3];
		}
		if (!$FC.defined(optionsMarginTop)) {
			chart.plotTop += axisOffset[0];
		}
		if (!$FC.defined(optionsMarginBottom)) {
			chart.marginBottom += axisOffset[2];
		}
		if (!$FC.defined(optionsMarginRight)) {
			chart.marginRight += axisOffset[1];
		}

		chart.setChartSize();

	},

	/**
	 * Add the event handlers necessary for auto resizing
	 *
	 */
	initReflow: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			renderTo = chart.renderTo,
			reflowTimeout;
			
		function reflow(e) {
			var width = optionsChart.width || $FC.adapterRun(renderTo, 'width'),
				height = optionsChart.height || $FC.adapterRun(renderTo, 'height'),
				target = e ? e.target : $FC.win; // #805 - MooTools doesn't supply e
				
			// Width and height checks for display:none. Target is doc in IE8 and Opera,
			// win in Firefox, Chrome and IE9.
			if (!chart.hasUserSize && width && height && (target === $FC.win || target === doc)) {
				
				if (width !== chart.containerWidth || height !== chart.containerHeight) {
					clearTimeout(reflowTimeout);
					chart.reflowTimeout = reflowTimeout = setTimeout(function () {
						if (chart.container) { // It may have been destroyed in the meantime (#1257)
							chart.setSize(width, height, false);
							chart.hasUserSize = null;
						}
					}, 100);
				}
				chart.containerWidth = width;
				chart.containerHeight = height;
			}
		}
		$FC.addEvent($FC.win, 'resize', reflow);
		$FC.addEvent(chart, 'destroy', function () {
			removeEvent($FC.win, 'resize', reflow);
		});
	},

	/**
	 * Resize the chart to a given width and height
	 * @param {Number} width
	 * @param {Number} height
	 * @param {Object|Boolean} animation
	 */
	setSize: function (width, height, animation) {
		var chart = this,
			chartWidth,
			chartHeight,
			fireEndResize;

		// Handle the isResizing counter
		chart.isResizing += 1;
		fireEndResize = function () {
			if (chart) {
				$FC.fireEvent(chart, 'endResize', null, function () {
					chart.isResizing -= 1;
				});
			}
		};

		// set the animation for the current process
		setAnimation(animation, chart);

		chart.oldChartHeight = chart.chartHeight;
		chart.oldChartWidth = chart.chartWidth;
		if ($FC.defined(width)) {
			chart.chartWidth = chartWidth = $FC.mathMax(0, $FC.mathRound(width));
			chart.hasUserSize = !!chartWidth;
		}
		if ($FC.defined(height)) {
			chart.chartHeight = chartHeight = $FC.mathMax(0, $FC.mathRound(height));
		}

		$FC.css(chart.container, {
			width: chartWidth + $FC.PX,
			height: chartHeight + $FC.PX
		});
		chart.setChartSize(true);
		chart.renderer.setSize(chartWidth, chartHeight, animation);

		// handle axes
		chart.maxTicks = null;
		$FC.each(chart.axes, function (axis) {
			axis.isDirty = true;
			axis.setScale();
		});

		// make sure non-cartesian series are also handled
		$FC.each(chart.series, function (serie) {
			serie.isDirty = true;
		});

		chart.isDirtyLegend = true; // force legend redraw
		chart.isDirtyBox = true; // force redraw of plot and chart border

		chart.getMargins();

		chart.redraw(animation);


		chart.oldChartHeight = null;
		$FC.fireEvent(chart, 'resize');

		// fire endResize and set isResizing back
		// If animation is disabled, fire without delay
		if (globalAnimation === false) {
			fireEndResize();
		} else { // else set a timeout with the animation duration
			setTimeout(fireEndResize, (globalAnimation && globalAnimation.duration) || 500);
		}
	},

	/**
	 * 设置统计图属性。在预渲染前后执行，用于确定边距尺寸。
	 */
	setChartSize: function (skipAxes) {
		var chart = this,
			inverted = chart.inverted,
			renderer = chart.renderer,
			chartWidth = chart.chartWidth,
			chartHeight = chart.chartHeight,
			optionsChart = chart.options.chart,
			spacingTop = optionsChart.spacingTop,
			spacingRight = optionsChart.spacingRight,
			spacingBottom = optionsChart.spacingBottom,
			spacingLeft = optionsChart.spacingLeft,
			clipOffset = chart.clipOffset,
			clipX,
			clipY,
			plotLeft,
			plotTop,
			plotWidth,
			plotHeight,
			plotBorderWidth;

		chart.plotLeft = plotLeft = $FC.mathRound(chart.plotLeft);
		chart.plotTop = plotTop = $FC.mathRound(chart.plotTop);
		chart.plotWidth = plotWidth = $FC.mathMax(0, $FC.mathRound(chartWidth - plotLeft - chart.marginRight));
		chart.plotHeight = plotHeight = $FC.mathMax(0, $FC.mathRound(chartHeight - plotTop - chart.marginBottom));

		chart.plotSizeX = inverted ? plotHeight : plotWidth;
		chart.plotSizeY = inverted ? plotWidth : plotHeight;
		
		chart.plotBorderWidth = plotBorderWidth = optionsChart.plotBorderWidth || 0;

		// Set boxes used for alignment
		chart.spacingBox = renderer.spacingBox = {
			x: spacingLeft,
			y: spacingTop,
			width: chartWidth - spacingLeft - spacingRight,
			height: chartHeight - spacingTop - spacingBottom
		};
		chart.plotBox = renderer.plotBox = {
			x: plotLeft,
			y: plotTop,
			width: plotWidth,
			height: plotHeight
		};
		clipX = $FC.mathCeil($FC.mathMax(plotBorderWidth, clipOffset[3]) / 2);
		clipY = $FC.mathCeil($FC.mathMax(plotBorderWidth, clipOffset[0]) / 2);
		chart.clipBox = {
			x: clipX, 
			y: clipY, 
			width: $FC.mathFloor(chart.plotSizeX - $FC.mathMax(plotBorderWidth, clipOffset[1]) / 2 - clipX), 
			height: $FC.mathFloor(chart.plotSizeY - $FC.mathMax(plotBorderWidth, clipOffset[2]) / 2 - clipY)
		};

		if (!skipAxes) {
			$FC.each(chart.axes, function (axis) {
				axis.setAxisSize();
				axis.setAxisTranslation();
			});
		}
	},

	/**
	 * 在自动边距应用前，初始化边距
	 */
	resetMargins: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			spacingTop = optionsChart.spacingTop,
			spacingRight = optionsChart.spacingRight,
			spacingBottom = optionsChart.spacingBottom,
			spacingLeft = optionsChart.spacingLeft;

		chart.plotTop = $FC.pick(chart.optionsMarginTop, spacingTop);
		chart.marginRight = $FC.pick(chart.optionsMarginRight, spacingRight);
		chart.marginBottom = $FC.pick(chart.optionsMarginBottom, spacingBottom);
		chart.plotLeft = $FC.pick(chart.optionsMarginLeft, spacingLeft);
		chart.axisOffset = [0, 0, 0, 0]; // top, right, bottom, left
		chart.clipOffset = [0, 0, 0, 0];
	},

	/**
	 * Draw the borders and backgrounds for chart and plot area
	 */
	drawChartBox: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			renderer = chart.renderer,
			chartWidth = chart.chartWidth,
			chartHeight = chart.chartHeight,
			chartBackground = chart.chartBackground,
			plotBackground = chart.plotBackground,
			plotBorder = chart.plotBorder,
			plotBGImage = chart.plotBGImage,
			chartBorderWidth = optionsChart.borderWidth || 0,
			chartBackgroundColor = optionsChart.backgroundColor,
			plotBackgroundColor = optionsChart.plotBackgroundColor,
			plotBackgroundImage = optionsChart.plotBackgroundImage,
			plotBorderWidth = optionsChart.plotBorderWidth || 0,
			mgn,
			bgAttr,
			plotLeft = chart.plotLeft,
			plotTop = chart.plotTop,
			plotWidth = chart.plotWidth,
			plotHeight = chart.plotHeight,
			plotBox = chart.plotBox,
			clipRect = chart.clipRect,
			clipBox = chart.clipBox;

		// Chart area
		mgn = chartBorderWidth + (optionsChart.shadow ? 8 : 0);

		if (chartBorderWidth || chartBackgroundColor) {
			if (!chartBackground) {
				
				bgAttr = {
					fill: chartBackgroundColor || $FC.NONE
				};
				if (chartBorderWidth) { // #980
					bgAttr.stroke = optionsChart.borderColor;
					bgAttr['stroke-width'] = chartBorderWidth;
				}
				chart.chartBackground = renderer.rect(mgn / 2, mgn / 2, chartWidth - mgn, chartHeight - mgn,
						optionsChart.borderRadius, chartBorderWidth)
					.attr(bgAttr)
					.add()
					.shadow(optionsChart.shadow);

			} else { // resize
				chartBackground.animate(
					chartBackground.crisp(null, null, null, chartWidth - mgn, chartHeight - mgn)
				);
			}
		}


		// Plot background
		if (plotBackgroundColor) {
			if (!plotBackground) {
				chart.plotBackground = renderer.rect(plotLeft, plotTop, plotWidth, plotHeight, 0)
					.attr({
						fill: plotBackgroundColor
					})
					.add()
					.shadow(optionsChart.plotShadow);
			} else {
				plotBackground.animate(plotBox);
			}
		}
		if (plotBackgroundImage) {
			if (!plotBGImage) {
				chart.plotBGImage = renderer.image(plotBackgroundImage, plotLeft, plotTop, plotWidth, plotHeight)
					.add();
			} else {
				plotBGImage.animate(plotBox);
			}
		}
		
		// Plot clip
		if (!clipRect) {
			chart.clipRect = renderer.clipRect(clipBox);
		} else {
			clipRect.animate({
				width: clipBox.width,
				height: clipBox.height
			});
		}

		// Plot area border
		if (plotBorderWidth) {
			if (!plotBorder) {
				chart.plotBorder = renderer.rect(plotLeft, plotTop, plotWidth, plotHeight, 0, plotBorderWidth)
					.attr({
						stroke: optionsChart.plotBorderColor,
						'stroke-width': plotBorderWidth,
						zIndex: 1
					})
					.add();
			} else {
				plotBorder.animate(
					plotBorder.crisp(null, plotLeft, plotTop, plotWidth, plotHeight)
				);
			}
		}

		// reset
		chart.isDirtyBox = false;
	},

	/**
	 * 检测是否某些属性，需要基于其设置和序列生成。
	 * 主要是chart.invert属性，扩展chart.angular和chart.polar属性
	 */
	propFromSeries: function () {
		var chart = this,
			optionsChart = chart.options.chart,
			klass,
			seriesOptions = chart.options.series,
			i,
			value;
			
			
		$FC.each(['inverted', 'angular', 'polar'], function (key) {
			
			// 默认的系列类型
			klass = $FC.seriesTypes[optionsChart.type || optionsChart.defaultSeriesType];
			
			// 获得统计图范围内可用的属性值
			value = (
				chart[key] || // 1. 之前设置的
				optionsChart[key] || // 2. 来着options的
				(klass && klass.prototype[key]) // 3. 来着默认系列类型的必须值
			);
	
			// 4. 来自设置的统计图系列的必须值
			i = seriesOptions && seriesOptions.length;
			while (!value && i--) {
				klass = $FC.seriesTypes[seriesOptions[i].type];
				if (klass && klass.prototype[key]) {
					value = true;
				}
			}
	
			// 设置统计图属性
			chart[key] = value;	
		});
		
	},

	/**
	 * Render all graphics for the chart
	 */
	render: function () {
		var chart = this,
			axes = chart.axes,
			renderer = chart.renderer,
			options = chart.options;

		var labels = options.labels,
			credits = options.credits,
			creditsHref;

		// Title
		chart.setTitle();


		// Legend
		chart.legend = new $FC.Legend(chart, options.legend);

		chart.getStacks(); // render stacks

		// Get margins by pre-rendering axes
		// set axes scales
		$FC.each(axes, function (axis) {
			axis.setScale();
		});

		chart.getMargins();

		chart.maxTicks = null; // reset for second pass
		$FC.each(axes, function (axis) {
			axis.setTickPositions(true); // update to reflect the new margins
			axis.setMaxTicks();
		});
		chart.adjustTickAmounts();
		chart.getMargins(); // second pass to check for new labels


		// Draw the borders and backgrounds
		chart.drawChartBox();		


		// Axes
		if (chart.hasCartesianSeries) {
			$FC.each(axes, function (axis) {
				axis.render();
			});
		}

		// The series
		if (!chart.seriesGroup) {
			chart.seriesGroup = renderer.g('series-group')
				.attr({ zIndex: 3 })
				.add();
		}
		$FC.each(chart.series, function (serie) {
			serie.translate();
			serie.setTooltipPoints();
			serie.render();
		});

		// Labels
		if (labels.items) {
			$FC.each(labels.items, function (label) {
				var style = $FC.extend(labels.style, label.style),
					x = $FC.pInt(style.left) + chart.plotLeft,
					y = $FC.pInt(style.top) + chart.plotTop + 12;

				// delete to prevent rewriting in IE
				delete style.left;
				delete style.top;

				renderer.text(
					label.html,
					x,
					y
				)
				.attr({ zIndex: 2 })
				.css(style)
				.add();

			});
		}

		// Credits
		if (credits.enabled && !chart.credits) {
			creditsHref = credits.href;
			chart.credits = renderer.text(
				credits.text,
				0,
				0
			)
			.on('click', function () {
				if (creditsHref) {
					location.href = creditsHref;
				}
			})
			.attr({
				align: credits.position.align,
				zIndex: 8
			})
			.css(credits.style)
			.add()
			.align(credits.position);
		}

		// Set flag
		chart.hasRendered = true;

	},

	/**
	 * Clean up memory usage
	 */
	destroy: function () {
		var chart = this,
			axes = chart.axes,
			series = chart.series,
			container = chart.container,
			i,
			parentNode = container && container.parentNode;
			
		// fire the chart.destoy event
		$FC.fireEvent(chart, 'destroy');
		
		// Delete the chart from charts lookup array
		$FC.charts[chart.index] = UNDEFINED;
		chart.renderTo.removeAttribute('data-highcharts-chart');

		// remove events
		removeEvent(chart);

		// ==== Destroy collections:
		// Destroy axes
		i = axes.length;
		while (i--) {
			axes[i] = axes[i].destroy();
		}

		// Destroy each series
		i = series.length;
		while (i--) {
			series[i] = series[i].destroy();
		}

		// ==== Destroy chart properties:
		$FC.each(['title', 'subtitle', 'chartBackground', 'plotBackground', 'plotBGImage', 
				'plotBorder', 'seriesGroup', 'clipRect', 'credits', 'pointer', 'scroller', 
				'rangeSelector', 'legend', 'resetZoomButton', 'tooltip', 'renderer'], function (name) {
			var prop = chart[name];

			if (prop && prop.destroy) {
				chart[name] = prop.destroy();
			}
		});

		// remove container and all SVG
		if (container) { // can break in IE when destroyed before finished loading
			container.innerHTML = '';
			removeEvent(container);
			if (parentNode) {
				discardElement(container);
			}

		}

		// clean it all up
		for (i in chart) {
			delete chart[i];
		}

	},


	/**
	 * VML的命名空间，在完成之前不能被添加。
	 */
	isReadyToRender: function () {
		var chart = this;

		// Note: in spite of JSLint's complaints, win == win.top is required
		/*jslint eqeq: true*/
		if ((!$FC.hasSVG && ($FC.win == $FC.win.top && $FC.doc.readyState !== 'complete')) || ($FC.useCanVG && !$FC.win.canvg)) {
		/*jslint eqeq: false*/
			if (useCanVG) {
				// 推迟渲染，直到canvg的库下载完成并准备好
				CanVGController.push(function () { chart.firstRender(); }, chart.options.global.canvasToolsURL);
			} else {
				$FC.doc.attachEvent('onreadystatechange', function () {
					$FC.doc.detachEvent('onreadystatechange', chart.firstRender);
					if ($FC.doc.readyState === 'complete') {
						chart.firstRender();
					}
				});
			}
			return false;
		}
		return true;
	},

	/**
	 * 所有数据加载完成后，准备进行首次渲染
	 */
	firstRender: function () {
		var chart = this,
			options = chart.options,
			callback = chart.callback;

		// 检查该统计图是否已经准备好渲染
		if (!chart.isReadyToRender()) {
			return;
		}

		// 创建显示容器
		chart.getContainer();

		// 在容器和渲染器建立后，运行一个早期初始化事件
		$FC.fireEvent(chart, 'init');

		
		chart.resetMargins();
		chart.setChartSize();

		// 从给定的系列中设置通用统计图属性（主要是反转属性）
		chart.propFromSeries();

		// get axes
		chart.getAxes();

		// Initialize the series
		$FC.each(options.series || [], function (serieOptions) {
			chart.initSeries(serieOptions);
		});

		// Run an event after axes and series are initialized, but before render. At this stage,
		// the series data is indexed and cached in the xData and yData arrays, so we can access
		// those before rendering. Used in Highstock. 
		$FC.fireEvent(chart, 'beforeRender'); 

		// depends on inverted and on margins being set
		chart.pointer = new $FC.Pointer(chart, options);

		chart.render();

		// add canvas
		chart.renderer.draw();
		// run callbacks
		if (callback) {
			callback.apply(chart, [chart]);
		}
		$FC.each(chart.callbacks, function (fn) {
			fn.apply(chart, [chart]);
		});
		
		
		// If the chart was rendered outside the top container, put it back in
		chart.cloneRenderTo(true);

		$FC.fireEvent(chart, 'load');

	}
}; // end Chart

//Hook for exporting module
$FC.Chart.prototype.callbacks = [];