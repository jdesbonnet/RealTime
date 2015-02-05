    function updateMap (camStatus) {
    	var i;
    	//alert ('camStatus.length=' + camStatus.length);
    	for (i = 0; i < camStatus.length; i++) {
    		var camera = cameras[camStatus[i].id];
    		
    		if (camera == null) {
    			continue;
    		}
    		
    		if (camStatus[i].status != camera.status) {
    		
    			// Update map
    			// Remove existing marker from map (if any)
    			if (camera.marker) {
    				map.removeOverlay(camera.marker);
    				camera.marker=null;
    			}
    			
    			var marker = createCameraMarker(camera,camStatus[i]);
				map.addOverlay(marker);
				
				camera.marker = marker;
				camera.status = camStatus[i].status;
				
				// Update left column
				
				var el = document.getElementById("leftColStatus_" + camera.id);
				if (camStatus[i].status == 1) {
					el.innerHTML="[Live]";
					YAHOO.util.Dom.removeClass(el,"camOffLine");
					YAHOO.util.Dom.addClass(el,"camLive");
				} else {
					el.innerHTML="[Off Line]";
					YAHOO.util.Dom.removeClass(el,"camLive");
					YAHOO.util.Dom.addClass(el,"camOffLine");
				}
				
					
    		}
    	}
    }
    
	function updateLeftCol (camStatus) {
    	for (var i = 0; i < camStatus.length; i++) {

			// Update left column
			var el = document.getElementById("leftColStatus_" + camStatus[i].id);
			
			if (! el) {
				continue;
			}
			
			if (camStatus[i].status == 1) {
				el.innerHTML="[Live]";
				YAHOO.util.Dom.removeClass(el,"camOffLine");
				YAHOO.util.Dom.addClass(el,"camLive");
			} else {
				el.innerHTML="[Off Line]";
				YAHOO.util.Dom.removeClass(el,"camLive");
				YAHOO.util.Dom.addClass(el,"camOffLine");
			}
    	}
    }
    
    
    function createCameraMarker (camera, cameraStatus) {
    	var marker = new YMarker (camera.point,
    		(cameraStatus.status == 1 ? cameraIcon : cameraErrorIcon)
    		);
    	marker.addAutoExpand(camera.id);
		YEvent.Capture(marker, EventsList.MouseClick, 
			function() {
				marker.openSmartWindow(makeBubbleHtml(camera));
			}
		);
		return marker;
    }
    
    
    function makeBubbleHtml (camera) {
    	var html = "";
    	html+="<img width=\"160\" height=\"120\" ";
    	html+="onClick=\"displayFullViewPanel('" + camera.id + "');\" ";
    	html+="src=\"" + camera.currentImageURL + "\" />";
    	html+="Status: " + (camera.status == 1 ? "on-line":"off-line");
    	return html;
    }
    
    /**
     * Display expaned (full res) view of webcam image
     */
    function displayFullViewPanel (cameraId) {
    	var camera = cameras[cameraId];
    	var panel = new YAHOO.widget.Panel("wcpanel", 
  			{ width:"700px", height: "550px", 
  			visible:true, 
  			draggable:true, 
  			close:true,
  			constraintoviewport:true, 
  			// anchor top-left of panel to top-left of content area
  			context: ["bd","tl","tl"] 
  			} );
	
		// Header is widget label (or id if no label)
		panel.setHeader(camera.id);
		var html = "";
		html += "<img src=\""+camera.currentImageURL+"\" /><br />";
		html += "<input type=\"button\" value=\"&lt;\" id=\"prevB\" />";
		html += "<input type=\"button\" value=\"&gt;\" id=\"nextB\" />";
		panel.setBody(html);
		//panel.setFooter("end");
		panel.render("bd");
    }
    
    function writeCarousel (carousel) {
    	//alert ('writeCarousel(): updating carousel');
    	var j = 0;
    	var i = 0;
    	
    	// Set carousel thumbnails and time
    	for (i = 0; i < carousel.images.length; i++) {
    		document.getElementById("ci" + j).src = carousel.images[i].url;
    		document.getElementById("t" + j).innerHTML = carousel.images[i].t;
    		j++;
    	}
    	
    	// Chose image mid-way in carousel to be selected
    	var selCol = carousel.images.length/2;
    	
    	// Set pointer for that carousel col visible
    	document.getElementById("pi" + selCol).style.visibility = "visible";
    	document.getElementById("ci" + selCol).src = carousel.images[selCol].url;
    	
    	// Set main image area
    	document.getElementById("mainImage").src=carousel.images[selCol].url;
    	
    	// Clear other pointers
    	for (i = 0; i < 7; i++) {
    		if (i != selCol) {
    			document.getElementById("pi" + i).style.visibility = "hidden";
    		}
    	}
    	
    	// Save selected position
    	selectedCarouselCol = selCol;
    }
 
    