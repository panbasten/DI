(function($){
	
	function _div(cls,text){
		if(text == undefined){
			text = "";
		}
		return $("<div class=\""+cls+"\">"+text+"</div>");
	}
	
	// 10进制数字转换为26进制A-Z
	function s10t26(n) {
		var s = "";
		while (n > 0) {
			var m = n % 26;
			if (m == 0){
				m = 26;
			}
			s = String.fromCharCode(m + 64) + s;
			n = (n - m) / 26;
		}
		return s;
	}
	
	// 26进制A-Z转换为10进制数字
	function s26t10(s) {
		if (s == "")
			return 0;
		var n = 0, j = 1;
		for ( var i = s.length - 1; i >= 0; i--) {
			var c = s[i].toUpperCase();
			if (c < "A" || c > "Z")
				return 0;
			n += (c.charCodeAt(0) - 64) * j;
			j *= 26;
		}
		return n;
	}
	
	// 获得单元格的尺寸
	function _getCellSize(sheetOpts,cidx,ridx){
		var m,mc;
		// 判断开始节点是否是合并节点
		for(var mergeName in sheetOpts.merge){
			m = sheetOpts.merge[mergeName];
			if(m){
				if(ridx>=m.ridx && ridx<=(m.ridx+m.rowspan-1)
					&& cidx>=m.cidx && cidx<=(m.cidx+m.colspan-1)){
					mc = m;
					break;
				}
			}
		}
		if(mc){
			return {
				w: _getColsWidth(sheetOpts,mc.cidx,(mc.cidx+mc.colspan-1)),
				h: _getRowsHeight(sheetOpts,mc.ridx,(mc.ridx+mc.rowspan-1)),
				cidx: mc.cidx,
				ridx: mc.ridx
			};
		}else{
			return {
				w: _getColWidth(sheetOpts,cidx),
				h: _getRowHeight(sheetOpts,ridx),
				cidx: cidx,
				ridx: ridx
			};
		}
		
	}
	
	// 获得列宽
	function _getColWidth(sheetOpts,colIdx){
		if(sheetOpts.colsWidth["c_"+colIdx] != undefined){
			return sheetOpts.colsWidth["c_"+colIdx];
		}else{
			return sheetOpts.defaultColWidth;
		}
	}
	
	// 获得列宽累计值
	function _getColsWidth(sheetOpts,sColIdx,eColIdx){
		var w = 0;
		for(var i=sColIdx;i<=eColIdx;i++){
			w = w + _getColWidth(sheetOpts,i);
		}
		return w;
	}
	
	// 获得行高
	function _getRowHeight(sheetOpts,rowIdx){
		if(sheetOpts.rowsHeight["r_"+rowIdx] != undefined){
			return sheetOpts.rowsHeight["r_"+rowIdx];
		}else{
			return sheetOpts.defaultRowHeight;
		}
	}
	
	// 获得行高累计值
	function _getRowsHeight(sheetOpts,sRowIdx,eRowIdx){
		var h = 0;
		for(var i=sRowIdx;i<=eRowIdx;i++){
			h = h + _getRowHeight(sheetOpts,i);
		}
		return h;
	}
	
	// 纵向滚动条
	function _initVsOC(target,parent,opts){
		var vsOC = _div("ui-spreadsheet-vsOC").appendTo(parent);
		var vsUp = $("<img class=\"ui-spreadsheet-vscroll-up\" width=\"17\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(vsOC);
		var vsOC_bg = _div("ui-spreadsheet-vscroll-bg").appendTo(vsOC);
		var vsOC_slider = _div("ui-spreadsheet-vscroll-slider").appendTo(vsOC_bg);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-top\" width=\"17\" height=\"4\" src=\""+opts.s_src+"\">").appendTo(vsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-vfill\" width=\"17\" height=\"1\" src=\""+opts.s_src+"\">").appendTo(vsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-vcenter\" width=\"17\" height=\"8\" src=\""+opts.s_src+"\">").appendTo(vsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-vfill\" width=\"17\" height=\"1\" src=\""+opts.s_src+"\">").appendTo(vsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-bottom\" width=\"17\" height=\"4\" src=\""+opts.s_src+"\">").appendTo(vsOC_slider);
		var vsDown = $("<img class=\"ui-spreadsheet-vscroll-down\" width=\"17\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(vsOC);
		
		var vs1OC = _div("ui-spreadsheet-vsOC ui-spreadsheet-vs1OC").appendTo(parent);
		
		vsUp.click(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			opts.vscrollHold = false;
			var sheetOpts = _getCurrentSheetOpts(opts);
			
			sheetOpts.top = sheetOpts.top - sheetOpts.defaultRowHeight;
			_resetVsOffset(target);
		});
		
		vsDown.click(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			opts.vscrollHold = false;
			var sheetOpts = _getCurrentSheetOpts(opts);
			
			sheetOpts.top = sheetOpts.top + sheetOpts.defaultRowHeight;
			_resetVsOffset(target);
		});
		
		vsOC_bg.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			opts.vscrollHold = false;
			
			var pos = Flywet.getMousePosition(e,vsOC_bg);
			var sheetOpts = _getCurrentSheetOpts(opts);
			var sTop = pos.y - parseInt(sheetOpts.vscrollFillH/2);
			
			_moveVsWithClick(target,opts,sTop);
		})
		.mousemove(function(e){
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
		})
		.mouseup(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
			
			opts.vscrollHold = false;
		});
		
		vsOC_slider.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			var pos = Flywet.getMousePosition(e);
			
			opts.vscrollHold = true;
			opts.scrollStartPosition = pos;
			
			var sheetOpts = _getCurrentSheetOpts(opts);
			sheetOpts.tempTop = sheetOpts.top;
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mousemove(function(e){
			
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mouseup(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
			
			opts.vscrollHold = false;
			
			e.stopPropagation();
            e.preventDefault();
		});
		
		
		vsOC.hide();
		vs1OC.hide();
		return [vsOC,vs1OC];
	}
	
	// 移动横向滚动条
	function _moveHs(target,opts,startPos,endPos){
		var sheetOpts = _getCurrentSheetOpts(opts),
			hscrollMaxLeft = opts.hscrollW - sheetOpts.hscrollFillW,
			paneMaxLeft = sheetOpts.allColumnW - opts.paneW;
		sheetOpts.left = sheetOpts.tempLeft + parseInt((endPos.x - startPos.x)*paneMaxLeft/hscrollMaxLeft);
		_resetHsOffset(target);
	}
	
	// 移动横向滚动条
	function _moveHsWithClick(target,opts,sLeft){
		var sheetOpts = _getCurrentSheetOpts(opts),
			hscrollMaxLeft = opts.hscrollW - sheetOpts.hscrollFillW,
			paneMaxLeft = sheetOpts.allColumnW - opts.paneW;
		sheetOpts.left = parseInt(sLeft*paneMaxLeft/hscrollMaxLeft);
		_resetHsOffset(target);
	}
	
	// 移动纵向滚动条
	function _moveVs(target,opts,startPos,endPos){
		var sheetOpts = _getCurrentSheetOpts(opts),
			vscrollMaxTop = opts.vscrollH - sheetOpts.vscrollFillH,
			paneMaxTop = sheetOpts.allRowH - opts.paneH;
		sheetOpts.top = sheetOpts.tempTop + parseInt((endPos.y - startPos.y)*paneMaxTop/vscrollMaxTop);
		_resetVsOffset(target);
	}
	
	// 点击移动纵向滚动条
	function _moveVsWithClick(target,opts,sTop){
		var sheetOpts = _getCurrentSheetOpts(opts),
			vscrollMaxTop = opts.vscrollH - sheetOpts.vscrollFillH,
			paneMaxTop = sheetOpts.allRowH - opts.paneH;
		sheetOpts.top = parseInt(sTop*paneMaxTop/vscrollMaxTop);
		_resetVsOffset(target);
	}
	
	// 横向滚动条
	function _initHsOC(target,parent,opts){
		var hs1OC = _div("ui-spreadsheet-hsOC ui-spreadsheet-hs1OC").appendTo(parent);
		
		var hsOC = _div("ui-spreadsheet-hsOC").appendTo(parent);
		var hsLeft = $("<img class=\"ui-spreadsheet-hscroll-left\" width=\"17\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC);
		var hsOC_bg = _div("ui-spreadsheet-hscroll-bg").appendTo(hsOC);
		var hsOC_slider = _div("ui-spreadsheet-hscroll-slider").appendTo(hsOC_bg);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-left\" width=\"4\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-hfill\" width=\"1\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-hcenter\" width=\"8\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-hfill\" width=\"1\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC_slider);
		$("<img class=\"ui-spreadsheet-gridScrollImg-el-right\" width=\"4\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC_slider);
		var hsRight = $("<img class=\"ui-spreadsheet-hscroll-right\" width=\"17\" height=\"17\" src=\""+opts.s_src+"\">").appendTo(hsOC);
		
		$("<div class=\"x-resizable-handle x-resizable-handle-west x-unselectable\" style=\"-moz-user-select: none; opacity: 0;\"></div>").appendTo(hsOC);
		
		hsLeft.click(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			opts.hscrollHold = false;
			var sheetOpts = _getCurrentSheetOpts(opts);
			
			sheetOpts.left = sheetOpts.left - sheetOpts.defaultColWidth;
			_resetHsOffset(target);
		});
		
		hsRight.click(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			opts.hscrollHold = false;
			var sheetOpts = _getCurrentSheetOpts(opts);
			
			sheetOpts.left = sheetOpts.left + sheetOpts.defaultColWidth;
			_resetHsOffset(target);
		});
		
		
		hsOC_bg.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			opts.hscrollHold = false;
			
			var pos = Flywet.getMousePosition(e,hsOC_bg);
			var sheetOpts = _getCurrentSheetOpts(opts);
			var sLeft = pos.x - parseInt(sheetOpts.hscrollFillW/2);
			
			_moveHsWithClick(target,opts,sLeft);
			
		})
		.mousemove(function(e){
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
		})
		.mouseup(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
			
			opts.hscrollHold = false;
		});
		
		hsOC_slider.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			var pos = Flywet.getMousePosition(e);
			
			opts.hscrollHold = true;
			opts.scrollStartPosition = pos;
			
			var sheetOpts = _getCurrentSheetOpts(opts);
			sheetOpts.tempLeft = sheetOpts.left;
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mousemove(function(e){
			
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mouseup(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
			
			opts.hscrollHold = false;
			
			e.stopPropagation();
            e.preventDefault();
		});
		
		hsOC.hide();
		hs1OC.hide();
		
		return [hsOC,hs1OC];
	}
	
	// 列头
	function _initColHdrs(target,parent,opts,sheetOpts){
		var gridColHdrsOC = _div("ui-spreadsheet-gridColHdrsOC").appendTo(parent);
		var gridColHdrsIC = _div("ui-spreadsheet-gridColHdrsIC").appendTo(gridColHdrsOC);
		
		var col,width;
		// 列头显示
		createColHdrs("ui-spreadsheet-gridColHdr","hc",true);
		
		// 列头选中
		createColHdrs("ui-spreadsheet-gridColHdr ui-spreadsheet-col-selected","hcs");
		
		// 列头激活
		createColHdrs("ui-spreadsheet-gridColHdr ui-spreadsheet-col-active","hca");
		
		function createColHdrs(cls,idPrefix,show){
			var left = 0;
			for(var i=0;i<sheetOpts.colNum;i++){
				width = _getColWidth(sheetOpts,i);
				col = _div(cls,s10t26(i+1)).appendTo(gridColHdrsIC);
				if(!show){
					col.hide();
				}
				col.css({
					left: left+"px",
					width: (width-5)+"px"
				}).attr("id",idPrefix+"_"+i);
				left = left + width;
			}
		}
		
		return gridColHdrsOC;
	}
	
	// 行头
	function _initRowHdrs(target,parent,opts,sheetOpts){
		var gridRowHdrsOC = _div("ui-spreadsheet-gridRowHdrsOC").appendTo(parent);
		var gridRowHdrsIC = _div("ui-spreadsheet-gridRowHdrsIC").appendTo(gridRowHdrsOC);
		
		var row,height,top = 0;
		// 行头显示
		createRowHdrs("ui-spreadsheet-gridRowHdr","hr",true);
		
		// 行头选中
		createRowHdrs("ui-spreadsheet-gridRowHdr ui-spreadsheet-row-selected","hrs");
		
		// 行头激活
		createRowHdrs("ui-spreadsheet-gridRowHdr ui-spreadsheet-row-active","hra");
		
		function createRowHdrs(cls,idPrefix,show){
			var top = 0;
			for(var i=0;i<sheetOpts.rowNum;i++){
				height = _getRowHeight(sheetOpts,i);
				row = _div(cls,(i+1)).appendTo(gridRowHdrsIC);
				if(!show){
					row.hide();
				}
				row.css({
					top: top+"px",
					height: (height-5)+"px",
					"line-height": (height-5)+"px"
				}).attr("id",idPrefix+"_"+i);
				top = top + height;
			}
		}
		
		return gridRowHdrsOC;
	}
	
	// 主表格区
	function _initPane(target,parent,opts,sheetOpts,saCell,colHdrs,rowHdrs){
		var opts = $.data(target, "spreadsheet").options;
		
		var paneOC = _div("ui-spreadsheet-paneOC").appendTo(parent);
		var paneIC = _div("ui-spreadsheet-paneIC").appendTo(paneOC);
		
		// 分隔线
		var col,width,left = 0;
		for(var i=0;i<sheetOpts.colNum;i++){
			width = _getColWidth(sheetOpts,i);
			col = _div("ui-spreadsheet-gridColSep").appendTo(paneIC);
			col.css({
				left: left+"px"
			});
			left = left + width;
		}
		
		// 行记录
		var row,cell,height,top=0;
		for(var i=0;i<sheetOpts.rowNum;i++){
			height = _getRowHeight(sheetOpts,i);
			row = _div("ui-spreadsheet-gridRow").appendTo(paneIC);
			row.css({
				top: top+"px",
				height: (height-5)+"px"
			}).attr("id","r_"+i);
			
			top = top + height;
		}
		
		// 选中框
		var bv1 = _div("ui-spreadsheet-defaultRangeBorder ui-spreadsheet-vertical-normal-default").appendTo(paneIC).hide();
		var bv2 = _div("ui-spreadsheet-defaultRangeBorder ui-spreadsheet-vertical-normal-default").appendTo(paneIC).hide();
		var bh1 = _div("ui-spreadsheet-defaultRangeBorder ui-spreadsheet-horizontal-normal-default").appendTo(paneIC).hide();
		var bh2 = _div("ui-spreadsheet-defaultRangeBorder ui-spreadsheet-horizontal-normal-default").appendTo(paneIC).hide();
		// 激活cell
		var ac = _div("ui-spreadsheet-activeCellElement ui-spreadsheet-cursorField").appendTo(paneIC).hide();
		
		
		// 选中的虚线拖拽框
		var fv1 = _div("ui-spreadsheet-fillRangeBorder ui-spreadsheet-vertical-normal-fill").appendTo(paneIC).hide();
		var fv2 = _div("ui-spreadsheet-fillRangeBorder ui-spreadsheet-vertical-normal-fill").appendTo(paneIC).hide();
		var fh1 = _div("ui-spreadsheet-fillRangeBorder ui-spreadsheet-horizontal-normal-fill").appendTo(paneIC).hide();
		var fh2 = _div("ui-spreadsheet-fillRangeBorder ui-spreadsheet-horizontal-normal-fill").appendTo(paneIC).hide();
		
		// 选择点
		var re = _div("ui-spreadsheet-rangeEdge ui-spreadsheet-cur-mdcurrd-1").appendTo(paneIC).hide();
		
		// 选中框背景
		var rbg = _div("ui-spreadsheet-rangeBackground").appendTo(paneIC).hide();
		
		var win = Flywet.getWindowScroll();
		var rm = _div("ui-spreadsheet-rangeMask").css({
			left: (win.width*(-1))+"px"
			,top: (win.height*(-1))+"px"
			,width: (win.width*2)+"px"
			,height: (win.height*2)+"px"
		}).appendTo(rbg);
		
		// 主区域事件
		paneIC.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			// 取得按下鼠标的坐标
			var pos = Flywet.getMousePosition(e,paneIC);
			var cpos = _getCellPositionByCoors(target,pos);
			
			opts.rangeHold = true;
			opts.vscrollHold = false;
			opts.hscrollHold = false;
			
			_showRange(cpos,cpos);
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mousemove(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			if(opts.rangeEdgeHold){
				var pos = Flywet.getMousePosition(e,paneIC);
				var cpos = _getCellPositionByCoors(target,pos);
				var d = (Math.abs(Flywet.cssNum(re,"left")-pos.x)>Math.abs(Flywet.cssNum(re,"top")-pos.y));
				_showFillRange(opts.rangeStartPosition,opts.rangeEndPosition,cpos,d);
			}
			else if(opts.rangeHold){
				var pos = Flywet.getMousePosition(e,paneIC);
				var cpos = _getCellPositionByCoors(target,pos);
				
				_showRange(opts.rangeStartPosition,cpos);
			}
			
			// 如果横向滚动条hold
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
			
			// 如果纵向滚动条hold
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
			
			e.stopPropagation();
            e.preventDefault();
		})
		.mouseup(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			if(opts.rangeEdgeHold){
				var pos = Flywet.getMousePosition(e,paneIC);
				var cpos = _getCellPositionByCoors(target,pos);
				var d = (Math.abs(Flywet.cssNum(re,"left")-pos.x)>Math.abs(Flywet.cssNum(re,"top")-pos.y));
				var fr = _showFillRange(opts.rangeStartPosition,opts.rangeEndPosition,cpos,d);
				
				opts.rangeEdgeHold = false;
				
				_showRange(fr.spos,fr.epos,opts.rangeType);
				_hideFillRange();
			}
			else if(opts.rangeHold){
				var pos = Flywet.getMousePosition(e,paneIC);
				var cpos = _getCellPositionByCoors(target,pos);
				
				opts.rangeHold = false;
				
				_showRange(opts.rangeStartPosition,cpos);
			}
			
			opts.vscrollHold = false;
			opts.hscrollHold = false;
			
			e.stopPropagation();
            e.preventDefault();
		});
		
		re.mousedown(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			opts.rangeEdgeHold = true;
			
			e.stopPropagation();
            e.preventDefault();
		});
		
		// 列头事件
		if(sheetOpts.showColHead){
			var colHdrsIC = colHdrs.find(".ui-spreadsheet-gridColHdrsIC");
			
			colHdrsIC.mousedown(function(e){
				// 确保是鼠标左键
				if (e.which != 1) {return;}
				
				var pos = Flywet.getMousePosition(e,colHdrsIC);
				var cpos = _getColPositionByCoors(target,pos);
				
				if(cpos.neer){
					opts.colResizeHeadHold = true;
				}else{
					opts.colHeadHold = true;
					_showColsRange(cpos.cidx,cpos.cidx);
				}
			})
			.mousemove(function(e){
				var pos = Flywet.getMousePosition(e,colHdrsIC);
				var cpos = _getColPositionByCoors(target,pos);
				
				if(opts.colHeadHold){
					_showColsRange(opts.rangeStartColIndex,cpos.cidx);
				}else if(opts.colResizeHeadHold){
					console.log("colResizeHeadHold");
				}else{
					_showColHeadCursor(cpos);
				}
				
				e.stopPropagation();
	            e.preventDefault();
			})
			.mouseup(function(e){
				// 确保是鼠标左键
				if (e.which != 1) {return;}
				
				var pos = Flywet.getMousePosition(e,colHdrsIC);
				var cpos = _getColPositionByCoors(target,pos);
				
				if(opts.colHeadHold){
					_showColsRange(opts.rangeStartColIndex,cpos.cidx);
					opts.colHeadHold = false;
				}else if(opts.colResizeHeadHold){
					
					opts.colResizeHeadHold = false;
				}
				
				e.stopPropagation();
	            e.preventDefault();
			});
		}
		
		// 行头事件
		if(sheetOpts.showRowHead){
			var rowHdrsIC = rowHdrs.find(".ui-spreadsheet-gridRowHdrsIC");
			
			rowHdrsIC.mousedown(function(e){
				// 确保是鼠标左键
				if (e.which != 1) {return;}
				
				var pos = Flywet.getMousePosition(e,rowHdrsIC);
				var rpos = _getRowPositionByCoors(target,pos);
				
				if(rpos.neer){
					opts.rowResizeHeadHold = true;
				}else{
					opts.rowHeadHold = true;
					_showRowsRange(rpos.ridx,rpos.ridx);
				}
				
				e.stopPropagation();
	            e.preventDefault();
			})
			.mousemove(function(e){
				var pos = Flywet.getMousePosition(e,rowHdrsIC);
				var rpos = _getRowPositionByCoors(target,pos);
				
				if(opts.rowHeadHold){
					_showRowsRange(opts.rangeStartRowIndex,rpos.ridx);
				}else if(opts.rowResizeHeadHold){
					console.log("rowResizeHeadHold");
				}else{
					_showRowHeadCursor(rpos);
				}
				
				e.stopPropagation();
	            e.preventDefault();
			})
			.mouseup(function(e){
				// 确保是鼠标左键
				if (e.which != 1) {return;}
				
				var pos = Flywet.getMousePosition(e,rowHdrsIC);
				var rpos = _getRowPositionByCoors(target,pos);
				
				if(opts.rowHeadHold){
					_showRowsRange(opts.rangeStartRowIndex,rpos.ridx);
					opts.rowHeadHold = false;
				}else if(opts.rowResizeHeadHold){
					
					opts.rowResizeHeadHold = false;
				}
				
				e.stopPropagation();
	            e.preventDefault();
			});
		}
		
		// 改变列头鼠标
		function _showColHeadCursor(cpos){
			var col = colHdrs.find("#hc_"+cpos.cidx);
			if(cpos.neer){
				$(col).addClass("ui-spreadsheet-col-resize");
			}else{
				$(col).removeClass("ui-spreadsheet-col-resize");
			}
		}
		
		// 改变行头鼠标
		function _showRowHeadCursor(rpos){
			var row = rowHdrs.find("#hr_"+rpos.ridx);
			if(rpos.neer){
				$(row).addClass("ui-spreadsheet-row-resize");
			}else{
				$(row).removeClass("ui-spreadsheet-row-resize");
			}
		}
		
		// 隐藏虚线框
		function _hideFillRange(){
			fv1.hide();
			fv2.hide();
			fh1.hide();
			fh2.hide();
		}
		
		// 显示虚线框
		function _showFillRange(spos, epos, pos, d){
			var sheetOpts = _getCurrentSheetOpts(opts);
			var spos_n = {
				ridx : Math.min(spos.ridx,epos.ridx)
				,cidx : Math.min(spos.cidx,epos.cidx)
			};
			var epos_n = {
				ridx : Math.max(spos.ridx,epos.ridx)
				,cidx : Math.max(spos.cidx,epos.cidx)
			};
			var startPos,endPos;
			var inset = false;
			// 内部
			if(pos.ridx>=spos_n.ridx && pos.ridx<=epos_n.ridx
				&& pos.cidx>=spos_n.cidx && pos.cidx<=epos_n.cidx){
				if(d){
					startPos = {
						ridx :spos_n.ridx
						,cidx : spos_n.cidx
					};
					endPos = {
						ridx : epos_n.ridx
						,cidx : pos.cidx
					};
				}else{
					startPos = {
						ridx : spos_n.ridx
						,cidx : spos_n.cidx
					};
					endPos = {
						ridx : pos.ridx
						,cidx : epos_n.cidx
					};
				}
				
				inset = true;
			}else{
				if(d){
					startPos = {
						ridx : spos_n.ridx
						,cidx : Math.min(spos_n.cidx,pos.cidx)
					};
					endPos = {
						ridx : epos_n.ridx
						,cidx : Math.max(epos_n.cidx,pos.cidx)
					};
				}else{
					startPos = {
						ridx : Math.min(spos_n.ridx,pos.ridx)
						,cidx : spos_n.cidx
					};
					endPos = {
						ridx : Math.max(epos_n.ridx,pos.ridx)
						,cidx : epos_n.cidx
					};
				}
			}
			
			var scss = _getCellCss(sheetOpts,startPos),
			ecss = _getCellCss(sheetOpts,endPos);
			
			fv1.css({
				left: (scss.left-1)+"px"
				,top: (scss.top-1)+"px"
				,width: "3px"
				,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)+2)+"px"
			}).show();
			
			var fv2Width = 3;
			if(inset && d){
				fv2Width = fv2Width + _getColsWidth(sheetOpts,(endPos.cidx+1),epos_n.cidx);
			}
			fv2.css({
				left: (ecss.left+_getColWidth(sheetOpts,endPos.cidx)-1)+"px"
				,top: (scss.top-1)+"px"
				,width: fv2Width+"px"
				,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)+2)+"px"
			}).show();
			
			fh1.css({
				left: (scss.left-1)+"px"
				,top: (scss.top-1)+"px"
				,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx))+"px"
				,height: "3px"
			}).show();
			
			var fh2Height = 3;
			if(inset && !d){
				fh2Height = fh2Height + _getRowsHeight(sheetOpts,(endPos.ridx+1),epos_n.ridx);
			}
			fh2.css({
				left: (scss.left-1)+"px"
				,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-1)+"px"
				,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)+3)+"px"
				,height: fh2Height+"px"
			}).show();
			
			return {spos:startPos,epos:endPos};
		}
		
		function _showRowsRange(srid,erid){
			var sheetOpts = _getCurrentSheetOpts(opts);
			var spos = {
				ridx : srid
				,cidx : 0
			};
			var epos = {
				ridx : erid
				,cidx : (sheetOpts.colNum-1)
			};
			_showRange(spos,epos,"row");
			opts.rangeStartRowIndex = srid;
			opts.rangeEndRowIndex = erid;
		}
		
		function _showColsRange(scid,ecid){
			var sheetOpts = _getCurrentSheetOpts(opts);
			var spos = {
				ridx : 0
				,cidx : scid
			};
			var epos = {
				ridx : (sheetOpts.rowNum-1)
				,cidx : ecid
			};
			_showRange(spos,epos,"col");
			opts.rangeStartColIndex = scid;
			opts.rangeEndColIndex = ecid;
		}
		
		// 显示选中框
		function _showRange(spos,epos,type){
			var sheetOpts = _getCurrentSheetOpts(opts);
			opts.rangeStartPosition = spos;
			opts.rangeEndPosition = epos;
			opts.rangeType = type || "cell";
			
			var startPos = {
				ridx : Math.min(spos.ridx,epos.ridx)
				,cidx : Math.min(spos.cidx,epos.cidx)
			};
			var endPos = {
				ridx : Math.max(spos.ridx,epos.ridx)
				,cidx : Math.max(spos.cidx,epos.cidx)
			};
			
			var m;
			// 判断开始节点是否是合并节点
			for(var mergeName in sheetOpts.merge){
				m = sheetOpts.merge[mergeName];
				if(m){
					if(startPos.ridx>=m.ridx && startPos.ridx<=(m.ridx+m.rowspan-1)
						&& startPos.cidx>=m.cidx && startPos.cidx<=(m.cidx+m.colspan-1)){
						startPos = {
							ridx : Math.min(startPos.ridx,m.ridx),
							cidx : Math.min(startPos.cidx,m.cidx)
						};
						break;
					}
				}
			}
			
			// 判断是否有合并单元格，扩展显示范围
			for(var mergeName in sheetOpts.merge){
				m = sheetOpts.merge[mergeName];
				if(m){
					// 判断内部节点是否是合并节点
					if(m.ridx>=startPos.ridx && m.ridx<=endPos.ridx
						&& m.cidx>=startPos.cidx && m.cidx<=endPos.cidx){
						endPos = {
							ridx : Math.max(endPos.ridx,(m.ridx+m.rowspan-1))
							,cidx : Math.max(endPos.cidx,(m.cidx+m.colspan-1))
						};
					}
				}
			}
			
			var scss = _getCellCss(sheetOpts,startPos),
				ecss = _getCellCss(sheetOpts,endPos);
				
			bv1.css({
				left: (scss.left-2)+"px"
				,top: (scss.top-1)+"px"
				,width: "5px"
				,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)+2)+"px"
			}).show();
			
			if(type == "col"){
				bv2.css({
					left: (ecss.left+_getColWidth(sheetOpts,endPos.cidx)-2)+"px"
					,top: (scss.top-2)+"px"
					,width: "5px"
					,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)+2)+"px"
				}).show();
			}else{
				bv2.css({
					left: (ecss.left+_getColWidth(sheetOpts,endPos.cidx)-2)+"px"
					,top: (scss.top-1)+"px"
					,width: "5px"
					,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)+2)+"px"
				}).show();
			}
			
			bh1.css({
				left: (scss.left-1)+"px"
				,top: (scss.top-2)+"px"
				,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)+2)+"px"
				,height: "5px"
			}).show();
			
			if(type == "row"){
				bh2.css({
					left: (scss.left-2)+"px"
					,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-2)+"px"
					,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)+2)+"px"
					,height: "5px"
				}).show();
			}else if(type == "col"){
				bh2.css({
					left: (scss.left-1)+"px"
					,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-2)+"px"
					,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)+2)+"px"
					,height: "5px"
				}).show();
			}else{
				bh2.css({
					left: (scss.left-1)+"px"
					,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-2)+"px"
					,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)+1)+"px"
					,height: "5px"
				}).show();
			}
			
			// 起始点标记
			var acSize = _getCellSize(sheetOpts,spos.cidx,spos.ridx),
				scss_o = _getCellCss(sheetOpts,{
					ridx: acSize.ridx,
					cidx: acSize.cidx
				});
			ac.css({
				left: scss_o.left+"px"
				,top: (scss_o.top+1)+"px"
				,width: (acSize.w-5)+"px"
				,height: (acSize.h-6)+"px"
			}).show();
			
			if(type=="row"){
				re.css({
					left: "-1px"
					,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-3)+"px"
				}).show();
			}else if(type=="col"){
				re.css({
					left: (ecss.left+_getColWidth(sheetOpts,endPos.cidx)-3)+"px"
					,top: "-1px"
				}).show();
			}else{
				re.css({
					left: (ecss.left+_getColWidth(sheetOpts,endPos.cidx)-3)+"px"
					,top: (ecss.top+_getRowHeight(sheetOpts,endPos.ridx)-3)+"px"
				}).show();
			}
			
			rbg.css({
				left: (scss.left+3)+"px"
				,top: (scss.top+3)+"px"
				,width: (_getColsWidth(sheetOpts,startPos.cidx,endPos.cidx)-5)+"px"
				,height: (_getRowsHeight(sheetOpts,startPos.ridx,endPos.ridx)-5)+"px"
			}).show();
			
			// 行头
			if(sheetOpts.showRowHead){
				rowHdrs.find(".ui-spreadsheet-row-active").hide();
				rowHdrs.find(".ui-spreadsheet-row-selected").hide();
				
				if(type=="row"){
					for(var i=startPos.ridx;i<=endPos.ridx;i++){
						$(rowHdrs.find("#hrs_"+i)).show();
					}
				}else{
					for(var i=startPos.ridx;i<=endPos.ridx;i++){
						$(rowHdrs.find("#hra_"+i)).show();
					}
				}
			}
			
			// 列头
			if(sheetOpts.showColHead){
				colHdrs.find(".ui-spreadsheet-col-active").hide();
				colHdrs.find(".ui-spreadsheet-col-selected").hide();
				if(type=="col"){
					for(var i=startPos.cidx;i<=endPos.cidx;i++){
						$(colHdrs.find("#hcs_"+i)).show();
					}
				}else{
					for(var i=startPos.cidx;i<=endPos.cidx;i++){
						$(colHdrs.find("#hca_"+i)).show();
					}
				}
			}
		}
		
		return paneOC;
	}
	
	// 获得鼠标点击位置的行坐标
	function _getRowPositionByCoors(target,pos){
		var opts = $.data(target, "spreadsheet").options;
		var sheetOpts = _getCurrentSheetOpts(opts);
		var ty = pos.y, ridx = 0;
		while(ty>0){
			ty = ty - _getRowHeight(sheetOpts, ridx);
			ridx++;
		}
		ridx--;
		
		ridx = Math.max(0,ridx);
		
		return {
			ridx : ridx
			,neer : (ridx!=0) && (Math.abs(ty)<=2 || (_getRowHeight(sheetOpts, ridx)+ty)<=2)
		};
	}
	
	// 获得鼠标点击位置的列坐标
	function _getColPositionByCoors(target,pos){
		var opts = $.data(target, "spreadsheet").options;
		var sheetOpts = _getCurrentSheetOpts(opts);
		var tx = pos.x, cidx = 0;
		while(tx>0){
			tx = tx - _getColWidth(sheetOpts, cidx);
			cidx++;
		}
		cidx--;
		
		cidx = Math.max(0,cidx);
		
		return {
			cidx : cidx
			,neer : (cidx!=0) && (Math.abs(tx)<=2 || (_getColWidth(sheetOpts, cidx)+tx)<=2)
		};
	}
	
	// 获得鼠标点击位置的Cell坐标
	function _getCellPositionByCoors(target,pos){
		var opts = $.data(target, "spreadsheet").options;
		var sheetOpts = _getCurrentSheetOpts(opts);
		var tx = pos.x, ty = pos.y, ridx = 0, cidx = 0;
		while(tx>0){
			tx = tx - _getColWidth(sheetOpts, cidx);
			cidx++;
		}
		while(ty>0){
			ty = ty - _getRowHeight(sheetOpts, ridx);
			ridx++;
		}
		
		return {
			ridx : Math.max(0,(ridx-1))
			,cidx : Math.max(0,(cidx-1))
		};
	}
	
	// 根据Cell坐标获得或者创建Cell对象
	function _getOrCreateCellByPosition(sheetOpts,sheet,pos){
		var row = sheet.find("#r_"+pos.ridx);
		var cell = $(row).find("#r_"+pos.ridx+"_c_"+pos.cidx);
		if(cell && cell.length>0){
			return cell;
		}else{
			cell = _div("ui-spreadsheet-gridCell");
			cell.attr("id","r_"+pos.ridx+"_c_"+pos.cidx);
			cell.data("position",{
				ridx: pos.ridx,
				cidx: pos.cidx
			});
			cell.data("merge",{
				ridx: pos.ridx,
				cidx: pos.cidx,
				colspan: 1,
				rowspan: 1
			});
			cell.appendTo(row);
			return cell;
		}
	}
	
	
	// 通过Cell坐标获得Cell
	function _getCellCss(sheetOpts,pos){
		return {
			top : _getRowsHeight(sheetOpts,0,(pos.ridx-1))
			,left : _getColsWidth(sheetOpts,0,(pos.cidx-1))
		};
	}
	
	function _rerenderSheet(target,parent,opts,sheetOpts,w,h){
		var sheet = _div("ui-spreadsheet-sheet").appendTo(parent);
		sheetOpts.sheet = sheet;
		
		// 左上角全选区
		if(sheetOpts.showColHead && sheetOpts.showRowHead){
			var saCell = _div("ui-spreadsheet-gridSelectAll").appendTo(sheet);
			sheetOpts.saCell = saCell;
		}
		
		// 列头
		if(sheetOpts.showColHead){
			var colHdrs = _initColHdrs(target,sheet,opts,sheetOpts);
			sheetOpts.colHdrs = colHdrs;
			sheetOpts.colHdrs.css({
				left: ((sheetOpts.showRowHead)?opts.headRowWidth:0)+"px"
				,width: ((sheetOpts.showRowHead)?(w-opts.headRowWidth):w)+"px"
			});
		}
		
		// 行头
		if(sheetOpts.showRowHead){
			var rowHdrs = _initRowHdrs(target,sheet,opts,sheetOpts);
			sheetOpts.rowHdrs = rowHdrs;
			sheetOpts.rowHdrs.css({
				top: ((sheetOpts.showColHead)?opts.headColHeight:0)+"px"
				,height: ((sheetOpts.showColHead)?(h-opts.headColHeight):h)+"px"
			});
		}
		
		// 主表格区
		var pane = _initPane(target,sheet,opts,sheetOpts,saCell,colHdrs,rowHdrs);
		sheetOpts.pane = pane;
		sheetOpts.pane.css({
			left: ((sheetOpts.showRowHead)?opts.headRowWidth:0)+"px"
			,top: ((sheetOpts.showColHead)?opts.headColHeight:0)+"px"
			,width: ((sheetOpts.showRowHead)?(w-opts.headRowWidth):w)+"px"
			,height: ((sheetOpts.showColHead)?(h-opts.headColHeight):h)+"px"
		});
		
		// 设置区域数据
		if(sheetOpts.region){
			sheetOpts.region = _setData(target,sheet,opts,sheetOpts);
		}
		
		sheet.hide();
		
		return sheet;
	}
	
	// 设置数据
	function _setData(target,sheet,opts,sheetOpts){
		var region = [];
		for(var i=0;i<sheetOpts.region.length;i++){
			var _r = _setRegionData(target,sheet,opts,sheetOpts,sheetOpts.region[i]);
			region.push(_r);
		}
		return region;
	}
	
	// 设置区域数据
	function _setRegionData(target,sheet,opts,sheetOpts,regionData){
		var region = regionData;
		if(region.regionObject && region.regionObject.type){
			// 表格区域
			if(region.regionObject.type == "TableRegion"){
				_setRegionTableRegion(target,sheet,opts,sheetOpts,region);
			}
		}
		
		return region;
	}
	
	
	/****************
	 * 表格区域
	 ****************/
	function _setRegionTableRegion(target,sheet,opts,sheetOpts,region){
		var regionObject = region.regionObject,
			regionData = regionObject.regionData;
		
		if(regionData && regionData.type){
			if(regionData.type == "PivotData"){
				_setRegionDataPivotData(target,sheet,opts,sheetOpts,region);
			}
		}
	}
	
	// 透视表类型数据解析
	function _setRegionDataPivotData(target,sheet,opts,sheetOpts,region){
		var regionObject = region.regionObject,
			regionData = regionObject.regionData.data,
			head = regionData.head,
			body = regionData.body,
			sp = region.startPosition,
			cidx = 0, ridx = 0;
		
		if(sp){
			cidx = sp.cidx;
			ridx = sp.ridx;
		}
		
		var r,cellOpts,cellObj,cn;
		if(head && head.row){
			// 头部行记录
			for(var i=0;i<head.row.length;i++,ridx++){
				r = head.row[i];
				cn = 0;
				for(var j=0;j<r.length;j++){
					cellOpts = $.extend({},$.fn.spreadsheet.cellStyleDefaults,r[j]);
					cellObj = _getOrCreateCellByPosition(sheetOpts,sheet,{cidx:cidx+cn,ridx:ridx});
					if(cellOpts["_TAG"] == "corner"){
						corner(cellOpts,cellObj);
					}else if(cellOpts["_TAG"] == "heading-heading"){
						headingHeading(cellOpts,cellObj);
					}else if(cellOpts["_TAG"] == "column-heading"){
						columnHeading(cellOpts,cellObj);
					}
					cn = cn + parseInt(cellOpts.colspan||1);
				}
			}
		}
		
		if(body && body.row){
			// 头部行记录
			for(var i=0;i<body.row.length;i++,ridx++){
				r = body.row[i];
				cn = 0;
				for(var j=0;j<r.length;j++){
					cellOpts = $.extend({},$.fn.spreadsheet.cellStyleDefaults,r[j]);
					cellObj = _getOrCreateCellByPosition(sheetOpts,sheet,{cidx:cidx+cn,ridx:ridx});
					if(cellOpts["_TAG"] == "row-heading"){
						rowHeading(cellOpts,cellObj);
					}else if(cellOpts["_TAG"] == "cell"){
						cell(cellOpts,cellObj);
					}
					
					cn = cn + parseInt(cellOpts.colspan||1);
				}
			}
		}
		
		// corner
		function corner(cellOpts,cell){
			cellOpts.border = {
				"top-style" : "thin"
				,"left-style" : "thin"
				,"bottom-style" : "thin"
				,"right-style" : "thin"
			};
			cellOpts.style = "ss-corner";
			_setRegionCellData(cellOpts,cell,"");
		}
		// heading-heading
		function headingHeading(cellOpts,cell){
			cellOpts.border = {
				"top-style" : "thin"
				,"left-style" : "thin"
				,"bottom-style" : "thin"
				,"right-style" : "thin"
			};
			cellOpts.style = "ss-heading-heading-"+cellOpts.style;
			cellOpts.indent = 0.5;
			cellOpts.fontWeight = "bolder"
			var val = cellOpts.caption.caption;
			_setRegionCellData(cellOpts,cell,val);
		}
		// column-heading
		function columnHeading(cellOpts,cell){
			cellOpts.border = {
				"top-style" : "thin"
				,"left-style" : "thin"
				,"bottom-style" : "thin"
				,"right-style" : "thin"
			};
			cellOpts.style = "ss-column-heading-"+cellOpts.style;
			cellOpts.fontWeight = "bolder"
			var val = cellOpts.caption.caption;
			_setRegionCellData(cellOpts,cell,val);
		}
		// row-heading
		function rowHeading(cellOpts,cell){
			cellOpts.border = {
				"top-style" : "thin"
				,"left-style" : "thin"
				,"bottom-style" : "thin"
				,"right-style" : "thin"
			};
			cellOpts.style = "ss-row-heading-"+cellOpts.style;
			cellOpts.fontWeight = "bolder"
			var val = cellOpts.caption.caption;
			if(cellOpts.drillExpand){
				val = "<input id='" + cellOpts.drillExpand.id 
					+ "' class='ss-cell-btn' type='image' title='展开' src='"
					+ opts.btn_src + cellOpts.drillExpand.img
					+ ".png'/>"+val;
			}
			_setRegionCellData(cellOpts,cell,val);
			
			// 事件
			if(cellOpts.drillExpand){
				cell.find("#"+cellOpts.drillExpand.id).mousedown(function(e){
					// TODO
					alert(cellOpts.drillExpand.id);
					e.stopPropagation();
		            e.preventDefault();
				});
			}
		}
		// cell
		function cell(cellOpts,cell){
			cellOpts.border = {
				"top-style" : "thin"
				,"left-style" : "thin"
				,"bottom-style" : "thin"
				,"right-style" : "thin"
			};
			cellOpts.style = "ss-cell-"+cellOpts.style;
			cellOpts.align = "right";
			var val = cellOpts.value;
			_setRegionCellData(cellOpts,cell,val);
		}
		
		// 设置单元格数据和样式
		function _setRegionCellData(style,cell,val){
			cell.data("style",style);
			
			if(style.style){
				cell.addClass(style.style);
			}
			
			// 合并单元格，包括边框
			_mergeCell(sheetOpts,sheet,cell,style);
			
			// 设置单元格的内容
			_setCellValue(sheetOpts,sheet,cell,val);
			
			// 设置字体
			_setCellFont(sheetOpts,sheet,cell,style);
			
			// 设置对齐方式
			_setCellAlign(sheetOpts,sheet,cell,style);
		}
	}
	
	// 设置单元格的内容
	function _setCellValue(sheetOpts,sheet,cell,val){
		var v = cell.find(".ui-spreadsheet-gridCell-val");
		if(v && v.length > 0){
			v.html(val);
		}else{
			v = _div("ui-spreadsheet-gridCell-val").appendTo(cell);
			v.css({
				width : (cell.width()-2) + "px"
				,"max-height" : cell.height() + "px"
			});
			v.html(val);
		}
		
	}
	
	// 设置字体
	function _setCellFont(sheetOpts,sheet,cell,style){
		var v = cell.find(".ui-spreadsheet-gridCell-val");
		if(style.fontFamily){
			v.css("font-family",style.fontFamily);
		}
		if(style.fontSize){
			v.css("font-size",style.fontSize);
		}
		if(style.fontColor){
			v.css("font-color",style.fontColor);
		}
		if(style.fontStyle){
			v.css("font-style",style.fontStyle);
		}
		if(style.fontWeight){
			v.css("font-weight",style.fontWeight);
		}
	}
	
	// 设置对齐方式
	function _setCellAlign(sheetOpts,sheet,cell,style){
		var v = cell.find(".ui-spreadsheet-gridCell-val"),
			valign = cell.find(".ui-spreadsheet-gridCell-valign");
		// 水平
		v.css("text-align",style.align);
		
		// 垂直
		if(style.valign == "top"){
			v.css("top","0");
			v.css("bottom",null);
			v.css("position","absolute");
			cell.append(v);
			valign.remove();
		}else if(style.valign == "bottom"){
			v.css("top",null);
			v.css("bottom","0");
			v.css("position","absolute");
			cell.append(v);
			valign.remove();
		}else {
			if(valign && valign.length > 0){
			}else{
				valign = _div("ui-spreadsheet-gridCell-valign");
			}
			v.css("top",null);
			v.css("bottom",null);
			v.css("position","relative");
			cell.append(valign);
			valign.append(v);
		}
		
		// 设置缩进
		if(style.align == "right"){
			v.css("margin-left",null);
			v.css("margin-right",style.indent+"em");
		}else{
			v.css("margin-left",style.indent+"em");
			v.css("margin-right",null);
		}
	}
	
	// 设置单元格的边框
	function _setCellBorder(sheetOpts,sheet,cell,style){
			
		var sp = cell.data("position"),
			merge = cell.data("merge");
		
		// 合并单元格的边框
		// 对于左上边框拥有下列属性
		// border.left-style 左边框样式
		// border.left-color 左边框颜色
		// border.left-owner 左边框是否表示当前单元格
		// border.left-proxy-owner 左边框代理所有者
		// 对于右下边框拥有下列属性
		// border.right-style 右边框样式
		// border.right-color 右边框颜色
		// border.right-proxy 右边框是否使用代理
		var border = $.extend({},cell.data("border"),style.border);
		cell.data("border",border);
		
		var borderStyle,lineStyle,
			left = _getColsWidth(sheetOpts,0,(sp.cidx-1)) + 1,
			top = 0;
		
		// 处理左边框
		if(border["left-style"] && border["left-style"] != "none"){
			lineStyle = $.fn.spreadsheet.borderLineStyle[border["left-style"]];
			borderStyle = lineStyle.width + "px " + lineStyle.style + " " 
				+ (border["left-color"]||lineStyle.color);
			left = left - lineStyle.width;
			cell.css("border-left",borderStyle);
			
			// 有此标志代表是代理
			if(style["left-proxy-owner"]){
				border["left-proxy-owner"] = style["left-proxy-owner"];
			}else{
				border["left-owner"] = true;
			}
		}
		
		// 处理上边框
		if(border["top-style"] && border["top-style"] != "none"){
			lineStyle = $.fn.spreadsheet.borderLineStyle[border["top-style"]];
			borderStyle = lineStyle.width + "px " + lineStyle.style + " " 
				+ (border["top-color"]||lineStyle.color);
			top = top - lineStyle.width;
			cell.css("border-top",borderStyle);
			
			// 有此标志代表是代理
			if(style["top-proxy-owner"]){
				border["top-proxy-owner"] = style["top-proxy-owner"];
			}else{
				border["top-owner"] = true;
			}
		}
		
		
		// 处理右边框，可能需要右侧的单元格代理边框
		if(style.border && style.border["right-style"]){
			// 判断是否可以代理
			if(checkProxyRight()){
				var rstyle = {
					"left-style" : style.border["right-style"]
				};
				if(style.border["right-color"]){
					rstyle["left-color"] = style.border["right-color"];
				}
				border["left-proxy"] = true;
				proxyRight({
					"left-proxy-owner" : merge
					,border : rstyle
				});
			}else{
				lineStyle = $.fn.spreadsheet.borderLineStyle[border["right-style"]];
				borderStyle = lineStyle.width + "px " + lineStyle.style + " " 
					+ (border["right-color"]||lineStyle.color);
				cell.css("border-right",borderStyle);
				border["left-proxy"] = false;
			}
		}
		
		// 处理下边框，可能需要下侧的单元格代理边框
		if(style.border && style.border["bottom-style"]){
			// 判断是否可以代理
			if(checkProxyTop()){
				var tstyle = {
					"top-style" : style.border["bottom-style"]
				};
				if(style.border["bottom-color"]){
					tstyle["top-color"] = style.border["bottom-color"];
				}
				border["top-proxy"] = true;
				proxyTop({
					"top-proxy-owner" : merge
					,border : tstyle
				});
			}else{
				lineStyle = $.fn.spreadsheet.borderLineStyle[border["bottom-style"]];
				borderStyle = lineStyle.width + "px " + lineStyle.style + " " 
					+ (border["bottom-color"]||lineStyle.color);
				cell.css("border-bottom",borderStyle);
				border["top-proxy"] = false;
			}
		}
		
		var cSize = _getCellSize(sheetOpts,sp.cidx,sp.ridx);
		cell.css({
			left: left+"px"
			,top: top+"px"
			,width: (cSize.w-1) +"px"
			,height: (cSize.h-1)+"px"
		});
		
		function checkProxyRight(){
			var temp_rowspan = merge.rowspan,
				temp_cell,
				temp_idx = merge.ridx;
			while(temp_rowspan>0){
				temp_cell = _getOrCreateCellByPosition(sheetOpts,sheet,{
					cidx: (merge.cidx + merge.colspan)
					,ridx : temp_idx
				});
				temp_idx = temp_idx + temp_cell.data("merge").rowspan;
				temp_rowspan = temp_rowspan - temp_cell.data("merge").rowspan;
			}
			return (temp_rowspan == 0);
		}
		
		function proxyRight(style){
			var temp_rowspan = merge.rowspan,
				temp_cell,
				temp_idx = merge.ridx;
			while(temp_rowspan>0){
				temp_cell = _getOrCreateCellByPosition(sheetOpts,sheet,{
					cidx: (merge.cidx + merge.colspan)
					,ridx : temp_idx
				});
				_setCellBorder(sheetOpts,sheet,temp_cell,style);
				
				temp_idx = temp_idx + temp_cell.data("merge").rowspan;
				temp_rowspan = temp_rowspan - temp_cell.data("merge").rowspan;
			}
		}
		
		function checkProxyTop(){
			var temp_colspan = merge.colspan,
				temp_cell,
				temp_idx = merge.cidx;
			while(temp_colspan>0){
				temp_cell = _getOrCreateCellByPosition(sheetOpts,sheet,{
					cidx: temp_idx
					,ridx : (merge.ridx + merge.rowspan)
				});
				temp_idx = temp_idx + temp_cell.data("merge").colspan;
				temp_colspan = temp_colspan - temp_cell.data("merge").colspan;
			}
			return (temp_colspan == 0);
		}
		
		function proxyTop(style){
			var temp_colspan = merge.colspan,
				temp_cell,
				temp_idx = merge.cidx;
			while(temp_colspan>0){
				temp_cell = _getOrCreateCellByPosition(sheetOpts,sheet,{
					cidx: temp_idx
					,ridx : (merge.ridx + merge.rowspan)
				});
				_setCellBorder(sheetOpts,sheet,temp_cell,style);
				
				temp_idx = temp_idx + temp_cell.data("merge").colspan;
				temp_colspan = temp_colspan - temp_cell.data("merge").colspan;
			}
		}
	}
	
	// 合并单元格
	function _mergeCell(sheetOpts,sheet,cell,style){
		var sp = cell.data("position"),
			id = "r_"+sp.ridx+"_c_"+sp.cidx,
			mergeOpts = {
				ridx: sp.ridx
				,cidx: sp.cidx
				,rowspan : parseInt(style.rowspan||1)
				,colspan : parseInt(style.colspan||1)
			};
		
		cell.data("merge",mergeOpts);
		
		if(mergeOpts.rowspan==1 && mergeOpts.colspan==1 && !sheetOpts.merge[id]){
		}else{
			var rowH = _getRowsHeight(sheetOpts,sp.ridx,(sp.ridx+mergeOpts.rowspan-1));
			var colW = _getColsWidth(sheetOpts,sp.cidx,(sp.cidx+mergeOpts.colspan-1));
			
			// 如果rowspan和colspan都为1，从merge中删除
			if(mergeOpts.rowspan==1 && mergeOpts.colspan==1){
				// TODO 将边框按位置分散到每个单元格上
				
				sheetOpts.merge[id] = undefined;
				cell.removeClass("ui-spreadsheet-mergedCell");
			}
			// 否则添加到merge中
			else{
				sheetOpts.merge[id] = mergeOpts;
				cell.addClass("ui-spreadsheet-mergedCell");
				
				// TODO 合并代理的边框
				// 对于边样式颜色相同的
				// 		如果都是owner的，采纳为合并单元格的样式
				// 		如果都是相同proxy，采纳为合并单元格的代理样式
				// 取消未采纳的代理

			}
		}
		
		// 设置边框
		_setCellBorder(sheetOpts,sheet,cell,style);
		
	}
	
	function _initSheets(target,parent,opts){
		return _div("ui-spreadsheet-sheets").appendTo(parent);
	}
	
	// sheet选择器
	function _initSelector(target,parent,opts){
		var selector = _div("ui-spreadsheet-sheetSelectorOC").appendTo(parent);
		var selectorTB = _div("ui-spreadsheet-sheetSelectorTB").appendTo(selector);
		_initToolbar(target,selectorTB,opts);
		
		var selectorIC = _div("ui-spreadsheet-sheetSelectorIC").appendTo(selector);
		var sheetTab = _initSelectorPane(target,selectorIC,opts);
		
		selector.hide();
		
		return [selector,sheetTab];
	}
	
	// selectorPane
	function _initSelectorPane(target,parent,opts){
		var tabpanel = _div("ui-spreadsheet-tabpanel").appendTo(parent);
		var tabpanelCt = _div("ui-spreadsheet-tabpanel-innerCt").appendTo(tabpanel);
		
		var sheetTab = [];
		
		for(var i=0;i<opts.sheet.length;i++){
			var tab = _initSelectorItem(target,tabpanelCt,opts.sheet[i],opts,i);
			sheetTab.push(tab);
		}
		return sheetTab;
	}
	
	// selector item
	function _initSelectorItem(target,parent,sheetOpts,opts,idx){
		var sheet = $("<div class='ui-spreadsheet-tabpanel-item'><em><button type='button'><span class='ui-spreadsheet-tab-inner'>"+sheetOpts.sheetName+"</span></button></em></div>");
		if(idx==opts.currentSheetIndex){
			sheet.addClass("ui-spreadsheet-tab-active");
		}
		sheet.data("tabIndex", idx);
		sheet.appendTo(parent);
		
		sheet.click(function(e){
			// 确保是鼠标左键
			if (e.which != 1) {return;}
			
			var idx = $(e.currentTarget).data("tabIndex");
			_shiftSheet(target, idx);
			
			e.preventDefault();
		});
		
		return sheet;
	}
	
	// toolbar
	function _initToolbar(target,parent,opts){
		var toolbar = _div("ui-spreadsheet-toolbar").appendTo(parent);
		var toolbarInnerCt = _div("ui-spreadsheet-toolbar-innerCt x-box-inner").appendTo(toolbar);
		
		var btns = [{
    		componentType : "fly:PushButton",
    		type : "button",
    		iconCls : "ui-icon-triangle-2-w-w"
    	},{
    		componentType : "fly:PushButton",
    		type : "button",
    		iconCls : "ui-icon-triangle-1-w"
    	},{
    		componentType : "fly:PushButton",
    		type : "button",
    		iconCls : "ui-icon-triangle-1-e"
    	},{
    		componentType : "fly:PushButton",
    		type : "button",
    		iconCls : "ui-icon-triangle-2-e-e"
    	}];
		Flywet.autocw(btns,toolbarInnerCt);
	}
	
	function _init(target){
		var ss_style = $(target).attr("style");
		
		$(target).addClass("ui-spreadsheet-f").attr("id",null).hide();
		
		var opts = $.data(target, "spreadsheet").options;
		
		// workspace
		var workspace = _div("ui-spreadsheet ui-spreadsheet-workspace x-border-box").insertAfter(target);
		workspace.attr("id", (opts.id)?(opts.id):"");
		if(ss_style){
			workspace.attr("style", ss_style);
		}
		
		// book
		var book = _div("ui-spreadsheet-book").appendTo(workspace);
		
		// vs
		var vs = _initVsOC(target,book,opts);
		
		// sheets
		var sheets = _initSheets(target,book,opts);
		
		// br_spacer
		var br = _div("ui-spreadsheet-gridBRSpacer").appendTo(book);
		br.hide();
		
		// selector
		var selector = _initSelector(target,book,opts);
		
		// hs
		var hs = _initHsOC(target,book,opts);
		
		workspace.bind("_resize", function() {
			var opts = $.data(target, "spreadsheet").options;
			if (opts.fit == true) {
				_rerender(target);
			}
			return false;
		});
		
		// 滚动条
		$(document).bind("mousedown.spreadsheet", function(e,d){
			opts.vscrollHold = false;
			opts.hscrollHold = false;
		});
		$(document).bind("mousemove.spreadsheet", function(e,d){
			// 如果横向滚动条hold
			if(opts.hscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveHs(target,opts,opts.scrollStartPosition,pos);
			}
			
			// 如果纵向滚动条hold
			if(opts.vscrollHold){
				var pos = Flywet.getMousePosition(e);
				_moveVs(target,opts,opts.scrollStartPosition,pos);
			}
		});
		$(document).bind("mouseup.spreadsheet", function(e,d){
			opts.vscrollHold = false;
			opts.hscrollHold = false;
		});
		
		return {
			workspace : workspace
			,book : book
			,vs : vs
			,sheets : sheets
			,selector : selector[0]
			,sheetTab : selector[1]
			,hs : hs
			,br : br
		};
	}
	
	// 计算显示尺寸
	function _calSize(target){
		var opts = $.data(target, "spreadsheet").options;
		
		var dim=Flywet.getElementDimensions(target);
		if(dim.css.width>0 && dim.css.height>0){
			dim = {width: dim.css.width, height: dim.css.height};
		}else{
			var parent=$(target).parent();
			if(parent.get(0).tagName === "BODY"){
				dim = Flywet.getWindowScroll();
				dim = {width: dim.width, height: dim.height};
			}else{
				dim = Flywet.getElementDimensions(parent);
				dim = {width: dim.css.width, height: dim.css.height};
			}
		}
		
		// 自定义尺寸，或者使用指定尺寸
		if(opts.width != "auto" && Flywet.isNumber(opts.width)){
			opts.width = parseInt(opts.width);
		}else{
			opts.width = dim.width;
		}
		
		if(opts.height != "auto" && Flywet.isNumber(opts.height)){
			opts.height = parseInt(opts.height);
		}else{
			opts.height = dim.height;
		}
		
		opts.workspaceWidth = opts.width - 2;
		opts.workspaceHeight = opts.height - 2;
		
		// 重置滚动条显示设置
		opts.showHScroll = false;
		opts.showVScroll = false;
		
		// 如果sheet多于1个，必须出现横向纵向滚动条
		if(_getSheetNum(opts)>1){
			opts.showHScroll = true;
			opts.showVScroll = true;
		}
		
		// 调整行列数
		for(var i=0;i<_getSheetNum(opts);i++){
			var sheetOpts = opts.sheet[i];
			// 用户指定的行列数
			sheetOpts.colNum = parseInt(sheetOpts.colNum);
			sheetOpts.rowNum = parseInt(sheetOpts.rowNum);
			
			// 对于没有设置行列数，采用默认的行列数
			var cpos = _getCellPositionByCoors(target, {
				x : _getPaneWidth(opts,sheetOpts),
				y : _getPaneHeight(opts,sheetOpts)
			});
			if(sheetOpts.colNum<1){
				sheetOpts.colNum = cpos.cidx + opts.offsetCellNumber;
			}
			if(sheetOpts.rowNum<1){
				sheetOpts.rowNum = cpos.ridx + opts.offsetCellNumber;
			}
		}
		
		// 对于只有一个sheet的情况，可以判断滚动条是否显示，如果多于1个sheet，则必须显示滚动条
		if(_getSheetNum(opts)==1){
			var sheetOpts = opts.sheet[0];
			// 判断是否显示滚动条 ,根据高度判断
			var allColsWidth = _getColsWidth(sheetOpts,0,(sheetOpts.colNum-1)),
				paneWidth = _getPaneWidth(opts,sheetOpts),
				allRowsHeight = _getRowsHeight(sheetOpts,0,(sheetOpts.rowNum-1)),
				paneHeight = _getPaneHeight(opts,sheetOpts);
			if(allColsWidth > paneWidth){
				opts.showHScroll = true;
			}
			if(allRowsHeight > paneHeight){
				opts.showVScroll = true;
			}
		}	
	}
	
	// 获得工作薄数量
	function _getSheetNum(opts){
		return opts.sheet.length;
	}
	
	// 获得当前sheet的opts
	function _getCurrentSheetOpts(opts){
		return opts.sheet[opts.currentSheetIndex];
	}
	
	// 获得当前sheet的opts
	function _getCurrentSheet(target){
		var sheet = $.data(target, "spreadsheet").sheet;
		return sheet.get(opts.currentSheetIndex);
	}
	
	// 切换Sheet页
	function _shiftSheet(target, sheetIdx, force){
		var ss = $.data(target, "spreadsheet"),
			opts = ss.options;
		
		if(opts.currentSheetIndex == sheetIdx && !force){
			return;
		}
		
		// 切换sheet页,切换选项卡
		if(opts.currentSheetIndex == sheetIdx){
			ss.sheet[sheetIdx].show();
			ss.sheetTab[sheetIdx].addClass("ui-spreadsheet-tab-active");
		}else{
			ss.sheet[opts.currentSheetIndex].hide();
			ss.sheet[sheetIdx].show();
			
			ss.sheetTab[opts.currentSheetIndex].removeClass("ui-spreadsheet-tab-active");
			ss.sheetTab[sheetIdx].addClass("ui-spreadsheet-tab-active");
			
			opts.currentSheetIndex = sheetIdx;
		}
		
		// vs
		if(opts.showVScroll){
			_resizeVs(target);
		}
		
		// hs
		if(opts.showHScroll){
			_resizeHs(target);
		}
	}
	
	// 获得表格有效区域的宽度
	function _getPaneWidth(opts,sheetOpts){
		var paneWidth = opts.workspaceWidth;
		if(sheetOpts.showRowHead){
			paneWidth = paneWidth - opts.headRowWidth;
		}
		if(opts.showVScroll){
			paneWidth = paneWidth - opts.vscrollWidth;
		}
		return paneWidth;
	}
	
	// 获得表格有效区域的宽度
	function _getPaneHeight(opts,sheetOpts){
		var paneHeight = opts.workspaceHeight;
		if(sheetOpts.showColHead){
			paneHeight = paneHeight - opts.headColHeight;
		}
		if(opts.showHScroll){
			paneHeight = paneHeight - opts.hscrollHeight;
		}
		return paneHeight;
	}
	
	// 调整纵向滚动条大小
	function _resizeVs(target){
		var ss = $.data(target, "spreadsheet"),
			opts = ss.options,
			sheetOpts = _getCurrentSheetOpts(opts),
			h = opts.vscrollHeight;
		
		var vscrollH = (h-opts.hscrollHeight*2),
			paneH = _getPaneHeight(opts,sheetOpts),
			allRowH = _getRowsHeight(sheetOpts,0,(sheetOpts.rowNum-1));
		
		opts.vscrollH = vscrollH;// 滚动条去头尾有效高度
		opts.paneH = paneH;// 显示主区域有效高度
		sheetOpts.allRowH = allRowH; // 当前所有行高度
		
		if(paneH>allRowH){
			ss.vs[1].height(h);
			ss.vs[0].hide();
			ss.vs[1].hide();
		}else{
			ss.vs[0].height(h);
			ss.vs[0].find(".ui-spreadsheet-vscroll-bg").height(vscrollH);
			var elh = vscrollH * paneH / allRowH;
			var vfillh = parseInt((elh-16)/2);
			vfillh = Math.max(vfillh,1);
			ss.vs[0].find(".ui-spreadsheet-gridScrollImg-el-vfill").height(vfillh);
			ss.vs[0].show();
			ss.vs[1].hide();
			
			// 滚动条填充高度
			sheetOpts.vscrollFillH = 16 + vfillh*2;
			
			_resetVsOffset(target);
		}
		
	}
	
	// 重置纵向滚动条浮动位置
	function _resetVsOffset(target){
		var ss = $.data(target, "spreadsheet"),
			opts = ss.options,
			sheetOpts = _getCurrentSheetOpts(opts),
			top = sheetOpts.top;
		
		var vscrollMaxTop = opts.vscrollH - sheetOpts.vscrollFillH,
			paneMaxTop = sheetOpts.allRowH - opts.paneH;
		
		top = Math.max(0,top);
		top = Math.min(paneMaxTop,top);
		sheetOpts.top = top;
		
		var sTop = parseInt(top * vscrollMaxTop / paneMaxTop);
		
		ss.vs[0].find(".ui-spreadsheet-vscroll-slider").css("top", sTop+"px");
		
		//pane
		sheetOpts.pane.find(".ui-spreadsheet-paneIC").css("top",(top*(-1))+"px");
		
		//rowHdrs
		if(sheetOpts.showRowHead){
			sheetOpts.rowHdrs.find(".ui-spreadsheet-gridRowHdrsIC").css("top",(top*(-1))+"px");
		}
		
	}
	
	// 调整横向滚动条大小
	function _resizeHs(target){
		var ss = $.data(target, "spreadsheet"),
			opts = ss.options,
			sheetOpts = _getCurrentSheetOpts(opts),
			w = opts.hscrollWidth - 2;
		
		var hscrollW = (w-opts.vscrollWidth*2),
			paneW = _getPaneWidth(opts,sheetOpts),
			allColumnW = _getColsWidth(sheetOpts,0,(sheetOpts.colNum-1));
		
		opts.hscrollW = hscrollW;// 滚动条去头尾有效宽度
		opts.paneW = paneW;// 显示主区域有效宽度
		sheetOpts.allColumnW = allColumnW; // 当前所有行宽度
		
		if(paneW>allColumnW){
			ss.hs[1].width(w);
			ss.hs[0].hide();
			ss.hs[1].show();
		}else{
			ss.hs[0].width(w);
			ss.hs[0].find(".ui-spreadsheet-hscroll-bg").width(hscrollW);
			var elh = hscrollW * paneW / allColumnW;
			var hfillw = parseInt((elh-16)/2);
			hfillw = Math.max(hfillw,1);
			ss.hs[0].find(".ui-spreadsheet-gridScrollImg-el-hfill").width(hfillw);
			ss.hs[0].show();
			ss.hs[1].hide();
			
			// 滚动条填充宽度
			sheetOpts.hscrollFillW = 16 + hfillw*2;
			
			_resetHsOffset(target);
		}
	}
	
	// 重置横向滚动条浮动位置
	function _resetHsOffset(target){
		var ss = $.data(target, "spreadsheet"),
			opts = ss.options,
			sheetOpts = _getCurrentSheetOpts(opts),
			left = sheetOpts.left;
	
		var hscrollMaxLeft = opts.hscrollW - sheetOpts.hscrollFillW,
			paneMaxLeft = sheetOpts.allColumnW - opts.paneW;
		
		left = Math.max(0,left);
		left = Math.min(paneMaxLeft,left);
		sheetOpts.left = left;
		
		var sLeft = parseInt(left * hscrollMaxLeft / paneMaxLeft);
		
		ss.hs[0].find(".ui-spreadsheet-hscroll-slider").css("left", sLeft+"px");
		
		//pane
		sheetOpts.pane.find(".ui-spreadsheet-paneIC").css("left",(left*(-1))+"px");
		
		//rowHdrs
		if(sheetOpts.showColHead){
			sheetOpts.colHdrs.find(".ui-spreadsheet-gridColHdrsIC").css("left",(left*(-1))+"px");
		}
	}
	
	function _rerender(target){
		var ss = $.data(target, "spreadsheet");
		var opts = ss.options;
		
		_calSize(target);
		
		// 总高度workspace
		var w = opts.workspaceWidth, 
			h = opts.workspaceHeight;
		ss.workspace.width(w).height(h);
		
		// 排除滚动条尺寸
		if(opts.showVScroll){
			w = w - opts.vscrollWidth;
		}
		if(opts.showHScroll){
			h = h - opts.hscrollHeight;
		}
		
		// vs
		if(opts.showVScroll){
			opts.vscrollHeight = h;
		}
		
		// hs
		if(opts.showHScroll){
			// 当多于1个sheet页时，要出现sheet页选择器
			if(_getSheetNum(opts)>1){
				// selector
				ss.selector.show();
				var selectorWidth = parseInt( w * 0.6 );
				ss.selector.width(selectorWidth);
				
				// hs
				opts.hscrollWidth = w-selectorWidth;
			}else{
				ss.selector.hide();
				// hs
				opts.hscrollWidth = w;
			}
		}
		
		// 右下角
		if(opts.showVScroll && opts.showHScroll){
			ss.br.show();
		}
		
		// sheets
		ss.sheets.width(w).height(h);
		ss.sheets.find(".ui-spreadsheet-sheet").width(w).height(h);
		
		// 多个Sheet页
		ss.sheets.empty();
		var sheet = [],
			sheetNum = _getSheetNum(opts);
		for(var sn=0;sn<sheetNum;sn++){
			var _sheet =  _rerenderSheet(target,ss.sheets,opts,opts.sheet[sn],w,h);
			sheet.push(_sheet);
		}
		ss.sheet = sheet;
		
		// 当前sheet页显示
		_shiftSheet(target,opts.currentSheetIndex,true);
		
	}
	
	function _show(target){
		var opts = $.data(target, "spreadsheet").options;
		var workspace = $.data(target, "spreadsheet").workspace;
		if (opts.onBeforeShow.call(target) == false) {
			return;
		}
		workspace.show();
		opts.hidden = false;
		opts.onShow.call(target);
	}
	
	function _hide(target){
		var opts = $.data(target, "spreadsheet").options;
		var workspace = $.data(target, "spreadsheet").workspace;
		if (opts.onBeforeHide.call(target) == false) {
			return;
		}
		workspace.hide();
		opts.hidden = true;
		opts.onHide.call(target);
	}
	
	$.fn.spreadsheet = function(options, param) {
		if(typeof options == "string"){
			return $.fn.spreadsheet.methods[options](this, param);
		}
		options = options || {};
		return this.each(function() {
			var t = $.data(this, "spreadsheet");
			var opts,sheet;
			if(t){
				opts = $.extend(t.options, options);
			}else{
				opts = $.extend(
						{},
						$.fn.spreadsheet.defaults,
						$.fn.spreadsheet.parseOptions(this),
						options
					);
				
				// 初始化sheet配置
				sheet = opts.sheet;
				if(!sheet){
					sheet = [{}];
				}
				
				for(var i=0;i<sheet.length;i++){
					sheet[i] = $.extend(
							{},
							$.fn.spreadsheet.sheetDefaults,
							sheet[i]
						);
				}
				opts.sheet = sheet;
				
				$.data(this, "spreadsheet", {
					options : 	opts
				});
				
				// 初始化页面元素
				t = _init(this);
				
				$.data(this, "spreadsheet", {
					options : 	opts
					,workspace : t.workspace
					,book : t.book
					,vs : t.vs
					,sheets : t.sheets
					,br : t.br
					,selector : t.selector
					,sheetTab : t.sheetTab
					,hs : t.hs
				});
				
				$.data(this, "componentType", "spreadsheet");
			}
			
			// 重新绘制
			t.workspace.css("display", "block");
			_rerender(this);
			
			
			if (opts.show) {
				_show(this);
			} else {
				_hide(this);
			}
			
		});
	};
	
	$.fn.spreadsheet.methods = {
		options : function(jq) {
			return $.data(jq[0], "spreadsheet").options;
		},
		workspace : function(jq) {
			return $.data(jq[0], "spreadsheet").workspace;
		}
	};
	
	$.fn.spreadsheet.parseOptions = function(target) {
		var t = $(target);
		return $.extend({},
			Flywet.parseOptions(target, ["id","width","height","show"])
		);
	};
	
	$.fn.spreadsheet.sheetDefaults = {
		
		defaultColWidth: 64		// 默认列宽
		,defaultRowHeight: 20 	// 默认行高
		
		// 是否显示表头
		,showColHead: true
		,showRowHead: true
		
		// 实际显示单元格数
		,rowNum: 0
		,colNum: 0
		
		// 滚动条起始cell位置
		,top : 0
		,left : 0
		
		// 样式
		,colsWidth : {}
		,rowsHeight : {}
		,merge : {}
	
	};
	
	$.fn.spreadsheet.cellStyleDefaults = {
		fontFamily : null
		,fontStyle : "normal" // normal, italic
		,fontWeight : "normal" // normal, lighter, bolder, bold
		,fontSize : null
		,fontColor : null
			
		,align : "left" // left, center, right, justify
		,valign : "middle" // top, middle, bottom
		,indent : 0 // 缩进值
		
		,colspan : 1
		,rowspan : 1
		
	};
	
	$.fn.spreadsheet.borderLineStyle = {
		// 无表格线
		none : {
			index : 0
			,width : 0
		}
		// 细线(1px)
		,thin : {
			index : 1
			,width : 1
			,style : "solid"
			,color : "#000"
		}
		// 中等粗线(2px)
		,medium : {
			index : 2
			,width : 2
			,style : "solid"
			,color : "#000"
		}
		// 虚线
		,dashed : {
			index : 3
			,width : 1
			,style : "dashed"
			,color : "#000"
		}
	};
	
	$.fn.spreadsheet.defaults = {
		id : 		null
		,width :	"auto"
		,height :	"auto"
		,fit :		true
		,show :		true
		
		,s_src : "resources/images/default/s.gif"
			
		,btn_src : "resources/images/report/"
		
		,headColHeight: 19 	// 列头高
		,headRowWidth: 41	// 行头宽
		
		,vscrollWidth: 17	// 纵向滚动条宽
		,vscrollHeight: 0	// 纵向滚动条高
		,hscrollHeight: 19	// 横向滚动条高
		,hscrollWidth: 0	// 横向滚动条宽
		
		// 是否显示滚动条，不能设置，由程序自己控制
//		,showHScroll: false 
//		,showVScroll: false
		
		,hscrollHold : false // 横向滚动条是否hold住
		,vscrollHold : false // 纵向滚动条是否hold住
		
		,offsetCellNumber: 3
		,currentSheetIndex: 0
		
		,rangeHold : false
		,rangeEdgeHold : false
		,rangeStartPosition : null
		,rangeEndPosition : null
		,rangeStartRowIndex : null
		,rangeEndRowIndex : null
		,rangeStartColIndex : null
		,rangeEndColIndex : null
		
		,rowHeadHold : false
		,colHeadHold : false
		,rowResizeHeadHold : false
		,colResizeHeadHold : false
		
		,onBeforeShow:	function(){}
		,onShow:	function(){}
		,onBeforeHide:	function(){}
		,onHide:	function(){}
	};
})(jQuery);


Flywet.widget.SpreadSheet = function(cfg){
	this.cfg = cfg;
	this.id = this.cfg.id;
	
	this.init();
};

Flywet.extend(Flywet.widget.SpreadSheet, Flywet.widget.BaseWidget);

Flywet.widget.SpreadSheet.prototype={
	init:function(){
		var cfg = {};
		$(Flywet.escapeClientId(this.id)).spreadsheet(cfg);
	}
};
