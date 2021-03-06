(function($) {
	
	function _init(target){
		var btn = $("<button type='button' class='btn'></button>").insertAfter(target);
		$(target).addClass("btn-original").hide().appendTo(btn);
		
		var opts = $.data(target, "pushbutton").options;
		
		var menuItems = opts.menuItems;
		
		// 设置ID
		btn.attr("id", (opts.id)?(opts.id):"");
		
		if(opts.title){
			btn.attr("title", opts.title);
		}
		
		var iconCls = opts.iconCls,
			iconAlign = opts.iconAlign,
			label = opts.label,
			clazz = "";
		
		if(opts.state == "disabled"){
			clazz = clazz + " disabled";
		}else if(opts.state == "active"){
			clazz = clazz + " active";
		}
		
		if(opts.btnStyle) {
			clazz = clazz + " btn-" + opts.btnStyle;
		}
		
		if(opts.btnSize) {
			if(opts.btnSize == "large"){ clazz = clazz + " btn-lg"; }
			else if(opts.btnSize == "small"){ clazz = clazz + " btn-sm"; }
			else if(opts.btnSize == "mini"){ clazz = clazz + " btn-xs"; }
		}
		
		btn.addClass(clazz);
		
		// 设置文字
		var content = "";
		if(label){
			content = label;
		}
		
		// 设置图片
		if(iconCls){
			if(iconAlign == "right"){
				content = content + "<span class='glyphicon "+iconCls+"'></span>";
			}else{
				content = "<span class='glyphicon "+iconCls+"'></span>" + content;
			}
		}
		
		if(opts.menuItems){
			content = content + "<span class='caret'></span>";
			
			// TODO
			
			console.log(opts.menuItems);
		}
		
		btn.html(content);
		
		// other event
		Flywet.attachBehaviors(btn,Flywet.assembleBehaviors(opts.events),opts);
		Flywet.attachBehaviorsOn(btn,opts);
		
		return btn;
	}
	
	function _toggle(target, state){
		var t = $.data(target, "pushbutton");
		if (state) {
		}else{
		}
	}
	
	$.fn.pushbutton = function(options, param) {
		if(typeof options == "string"){
			return $.fn.pushbutton.methods[options](this, param);
		}
		options = options || {};
		return this.each(function() {
			var t = $.data(this, "pushbutton");
			if(t){
				$.extend(t.options, options);
			}else{
				$.data(this, "pushbutton", {
					options : $.extend(
							{},
							$.fn.pushbutton.defaults,
							$.fn.pushbutton.parseOptions(this),
							options
						)
				});
			}
			var btn = _init(this);
			$.data(this, "pushbutton", {button:btn} );
			
		});
	};
	
	$.fn.pushbutton.methods = {
		options : function(jq) {
			return $.data(jq[0], "pushbutton").options;
		},
		enable : function(jq) {
			return jq.each(function(){
				_toggle(this, false);
			});
		},
		disable : function(jq) {
			return jq.each(function(){
				_toggle(this, true);
			});
		}
	};
	
	$.fn.pushbutton.parseOptions = function(target) {
		var t = $(target);
		return $.extend(
				{},
				Flywet.parseOptions(target, ["id", "state", "title", "label", "btnStyle", "iconCls", "iconAlign"])
			);
	};
	
	$.fn.pushbutton.defaults = {
		id : null,
		label : null,
		btnStyle : "default",//default,primary,success,info,warning,danger,link
		btnSize : "default",// default,large,small,mini
		iconCls : null,
		iconAlign : "left"
	};
	
})(jQuery);


Flywet.widget.PushButton=function(cfg){
	this.cfg = cfg;
	this.id = this.cfg.id;
	this.jqId = Flywet.escapeClientId(this.id);
	
	this.init();
};

Flywet.extend(Flywet.widget.PushButton, Flywet.widget.BaseWidget);

Flywet.widget.PushButton.prototype.init = function() {
	if(this.cfg.parent || this.cfg.parentId){
		this.parent = this.cfg.parent || $(Flywet.escapeClientId(this.cfg.parentId));
		this.jq = $(this.parent).find(this.jqId);
		if(this.jq.length == 0){
			this.jq = $("<span></span>");
		}
		this.parent.append(this.jq);
	}else{
		this.jq = $(this.jqId);
	}
	
	this.jq.pushbutton(this.cfg);
};

Flywet.widget.PushButton.prototype.isActive = function(){
	return this.jq.hasClass("active");
};

Flywet.PushButton = {
	showMenu : function(menuId, target){
		var $E = $(target),
			x = $E.offset().left,
			y = $E.offset().top,
			w = $E.outerHeight();
		window[menuId+'_var'].show({x:x,y:y+w});
	},
	destroy : function(menuId){
		window[menuId+'_var'].destroy();
	}
};







Flywet.widget.PushButtonGroup=function(cfg){
	this.cfg = cfg;
	this.id = this.cfg.id;
	this.jqId = Flywet.escapeClientId(this.id);
	
	this.init();
};

Flywet.extend(Flywet.widget.PushButtonGroup, Flywet.widget.BaseWidget);

Flywet.widget.PushButtonGroup.prototype.init = function() {
	if(this.cfg.parent || this.cfg.parentId){
		this.parent = this.cfg.parent || $(Flywet.escapeClientId(this.cfg.parentId));
		this.jq = $(this.parent).find(this.jqId);
		if(this.jq.length == 0){
			this.jq = $("<div></div>");
			this.jq.addClass("btn-group");
		}
		this.parent.append(this.jq);
	}else{
		this.jq = $(this.jqId);
	}
	
	if(this.cfg.subs){
		for(var i=0;i<this.cfg.subs.length;i++){
			console.log(this.cfg.subs[i]);
			Flywet.autocw(this.cfg.subs[i], this.jq);
		}
	}
};