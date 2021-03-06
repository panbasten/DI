/**
 * 浏览面板
 * yanghyc
 */
Flywet.widget.BrowsePanel=function(cfg){
	this.cfg = cfg;
	this.id = this.cfg.id;
	this.jqId = Flywet.escapeClientId(this.id);
	this.jq = $(this.jqId);	
	this.container;
	this.init();
	this.flush(cfg);
};
Flywet.extend(Flywet.widget.BrowsePanel, Flywet.widget.BaseWidget);
Flywet.widget.BrowsePanel.prototype={
	init:function(){
		if(!this.container){
			if(this.jq.hasClass("ui-browsepanel-con")){
				this.container = this.jq;
			}else{
				this._createContainer();
				this.jq.append(this.container);
			}
		};
		if(this.cfg.attrs && this.cfg.attrs.style){
			this.container.css(this.cfg.attrs.style);
		}
		if(this.cfg.attrs && this.cfg.attrs["class"]){
			this.container.addClass(this.cfg.attrs["class"]);
		}
		return this.container;
	},
	_createContainer:function(){
		this.container=$("<div></div>");
		this.container.addClass('ui-browsepanel-con');
		this.addItem(this.cfg.items);
	},
	getSelections:function(){
		var items = [];
		this.container.children().each(function(i){
			if($(this).hasClass("active")){
				items.push($(this).data("data"));
			}
		});
		return items;
	},
	getItem:function(id){
		return this.container.find(Flywet.escapeClientId(id));
	},
	addItem:function(items){
		if(!items) return;
		var _self=this;
		
		for(var i=0;i<items.length;i++){
			var item=items[i];
			if(!item.attrs) item.attrs = {};
			if(!item.attrs.events) item.attrs.events = {};
			
			// init
			var itemDom=$('<div></div>');
			itemDom.data("data",$.extend({},item.data,item.attrs));
			itemDom.attr("title",(item.attrs.title||item.attrs.displayName));
			var cls = "ui-browsepanel-itemcon";
			if(item.attrs["class"]){
				cls = cls + " " + item.attrs["class"];
			}
			itemDom.addClass(cls);
			itemDom.attr({
				"id":item.attrs.type+":"+item.id,
				"oncontextmenu":"return false"
			});
			var icon=$('<div></div>');
			icon.addClass('ui-browsepanel_itemicon ui-browsepanel-'+(item.attrs.type?item.attrs.type:'leaf'));				
			var span=$('<span></span>');
			var p=$('<p></p>');
			var pHtml = item.attrs.displayName;
			if(item.attrs.lastTime){
				pHtml = pHtml + "<br/><font class='ui-browsepanel-item-lasttime'>" + item.attrs.lastTime + "</font>";
			}
			p.html(pHtml);
			span.append(p);
			itemDom.append(icon);
			itemDom.append(span);
			
			this.container.append(itemDom);
			
			// click event
			itemDom.bind("click",item,function(event){
				var el = $(this),
					active = el.is(".active");
				el.parent().children().removeClass("active");
				if(!active)
					el.addClass("active");
				if(event.data && event.data.attrs && event.data.attrs.events && event.data.attrs.events["click"]){
					Flywet.invokeFunction(event.data.attrs.events["click"],event,event.data);
				}
			});
			
			// mouseover event
			itemDom.bind("mouseover",item,function(event){
				$(this).addClass("hover");
				if(event.data && event.data.attrs && event.data.attrs.events && event.data.attrs.events["mouseover"]){
					Flywet.invokeFunction(event.data.events["mouseover"],event,event.data);
				}
			});
			
			// mouseout event
			itemDom.bind("mouseout",item,function(event){
				$(this).removeClass("hover");
				if(event.data && event.data.attrs && event.data.attrs.events && event.data.attrs.events["mouseout"]){
					Flywet.invokeFunction(event.data.events["mouseout"],event,event.data);
				}
			});
			
			// other event
			Flywet.attachBehaviors(itemDom,Flywet.assembleBehaviors(item.attrs.events,["click","mouseover","mouseout"]),item);
			
			if(item.attrs.iconStyle){
				$(icon).addClass(item.attrs.iconStyle);
			};
			if(item.attrs.icon){
				$(icon).css({
					'background':'url('+item.attrs.icon+') no-repeat'
				});
			};
			if(item.attrs.draggable){
				new Flywet.widget.Drag({
        			id:item.id,
        			dom:itemDom,
        			targetid:item.attrs.targetid,
        			data:item
        		});
			}
		}
					
	},
	flush:function(cfg){
		if(!cfg) return;
		this.cfg = cfg;
		this.container.html('');
		this.addItem(cfg.items);
	},
	getCurrentData:function() {
		return this.cfg.attrs;
	}
};