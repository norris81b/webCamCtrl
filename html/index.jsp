<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.onetouchis.camctrl.PresetData" %>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<meta name="author" content="">

<title>Remote Camera Controller</title>
<style type="text/css">
    body { margin: 0; }
    #shade, #modal { display: none; }
    #shade { position: fixed; z-index: 100; top: 0; left: 0; width: 100%; height: 100%; }
    #modal { position: fixed; z-index: 101; top: 45%; left: 33%; width: 50%; }
    #shade { background: silver; opacity: 0.5; filter: alpha(opacity=50); }
</style>

<script src="js/jquery-1.11.0.js"></script>
<script>
    <!-- indicates the next preset push should be treated as a "set the preset" command -->
    var setPreset;
    
    <!-- indicates which preset to set -->
    var presetToSet;
    
    <!-- indicates whether or not scanning is active -->
    var isScanning;
    
    <!-- an array of the preset number and text values -->
    var presetData;
    
    <!-- sends the updated preset information and hides the popup -->
    function sendPresetAndClear(elem) 
    {
                var presetText = document.getElementById("presetName");
                // TODO presetText is an HTMLTextAreaElement
                sendCmd(presetToSet + "::" + presetText.value);
                modal.style.display=shade.style.display= 'none';
                setPreset = false;
    }
    
    <!-- builds a single preset -->
	function buildPreset(presetNumber, presetText)
	{
       document.getElementById("preset" + presetNumber).innerHTML = 
           "<div class=\"col-xs-2\"><button onclick=\"sendCmd(" + presetNumber + ")\" type=\"button\" class=\"btn btn-primary\">" + presetText + "</button></div>";
	}
	
	<!-- builds all of the presets -->
	function presets()
	{
		for (var i=0;i<10;i++)
		{ 
			buildPreset(i, "Preset " + i);
		}
	}

    <!-- starts and stops scanning from preset to preset -->	
	function sendScanning(elem)
	{
	  if ("Stop Scanning" == elem.value)
	  {
	    elem.value = "Scan Presets";
	    elem.textContent = "Scan Presets";
	    sendCmd("SCAN_PRESETS::OFF");
	  }
	  else
	  {
	    elem.value = "Stop Scanning";
	    elem.textContent = "Stop Scanning";
	    sendCmd("SCAN_PRESETS::ON");
	  }
	}
	
	<!-- most buttons send their command to this function -->
	function sendCmd(cmd)
	{
		var cmdObject = new Object();
		cmdObject.command = cmd;
		if (setPreset && (cmd >= 0 && cmd <=9))
		{
	   		var presetText = document.getElementById("presetName");
            presetText.value = presetData[cmd].text
	   		
			presetToSet = cmd;
		    modal.style.display=shade.style.display= 'block';
		}
		else
		{
			if ('PRESET_STORE' == cmd)
			{
				setPreset = true;
			}
			var jsonData = JSON.stringify(cmdObject);
	   		$('#result').html(jsonData);
			$.getJSON("ptzcamera", {action:"export",json:jsonData}, function(data) {
			  if (data.PRESET_DATA) {
	   			presetData = $.parseJSON(data.PRESET_DATA);
		   		for (var i=0;i<10;i++)
				{ 
					buildPreset(presetData[i].number, presetData[i].text);
				}
	   		  }
			});
		}
	}
	
</script>

<!-- Bootstrap core CSS -->
<link href="css/bootstrap.min.css" rel="stylesheet">

<!-- Custom styles for this template -->
<link href="css/grid.css" rel="stylesheet">

<!-- Just for debugging purposes. Don't actually copy this line! -->
<!--[if lt IE 9]><script src="../../assets/js/ie8-responsive-file-warning.js"></script><![endif]-->

<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
<!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>

<body onload="sendCmd('PRESETS')">
    <div id="shade"></div>
    <div id="modal">
        <p>Enter new name for preset</p>
        <textarea id="presetName" rows="1" cols="25"></textarea>
        <button id="close" onclick="sendPresetAndClear(this.parentNode)">Ok</button>
    </div>

	<div class="row" style="align: center; fixed-width: 1000;">
		<div class="col-xs-12" align="center">
			<div class="row">
				<div class="col-xs-12">
					<div class="row" style="box-shadow: 10px 10px 5px #888888;">
						<div class="col-xs-4"><h3>Remote Camera Controller</h3></div>
						<div class="col-xs-4">
						</div>
						<div class="col-xs-4"></div>
					</div>
					<div class="row" style="box-shadow: 5px 5px 5px #888888;">
						<div class="col-xs-4"></div>
						<div class="col-xs-4"><h4>Presets</h4></div>
						<div class="col-xs-4"></div>
					</div>
					<div class="row">
						<div class="col-xs-2"><button id="scanButton" onclick="sendScanning(this)" type="button" class="btn btn-primary">Scan Presets</button></div>
						<div id="preset0"></div>
						<div id="preset1"></div>
						<div id="preset2"></div>
						<div id="preset3"></div>
						<div id="preset4"></div>
					</div>
					<div class="row">
						<div class="col-xs-2"><button onclick="sendCmd('PRESET_STORE')" type="button" class="btn btn-primary">Set Preset</button></div>
						<div id="preset5"></div>
						<div id="preset6"></div>
						<div id="preset7"><p>Loading Presets ...</p></div>
						<div id="preset8"></div>
						<div id="preset9"></div>
					</div>
				</div>
			</div>
			<br/>
			<div class="row" style="box-shadow: 5px 5px 5px #888888;">
				<div class="col-xs-4"></div>
				<div class="col-xs-4"><h4>Manual Control</h4></div>
				<div class="col-xs-4"></div>
			</div>

			<div class="row" style="align: center">
				<div class="col-xs-3" align="center">
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('HOME_POSITION_MOVE')"  type="button" class="btn btn-primary">Home</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12"></div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('DOC_POSITION_MOVE')"  type="button" class="btn btn-primary">Document</button>
						</div>
					</div>
				</div>
				<div class="col-xs-3" align="center">
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('TILT_UP_START')" onmouseup="sendCmd('PAN_TILT_STOP')" type="button" class="btn btn-primary">Up</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-4" align="center">
							<button onmousedown="sendCmd('PAN_LEFT_START')" onmouseup="sendCmd('PAN_TILT_STOP')" type="button" class="btn btn-primary">Left&nbsp</button>
						</div>
						<div class="col-xs-4" align="center">
						</div>
						<div class="col-xs-4" align="center">
							<button onmousedown="sendCmd('PAN_RIGHT_START')" onmouseup="sendCmd('PAN_TILT_STOP')" type="button" class="btn btn-primary">Right</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12" align="center">
							<button onmousedown="sendCmd('TILT_DOWN_START')" onmouseup="sendCmd('PAN_TILT_STOP')" type="button" class="btn btn-primary">Down</button>
						</div>
					</div>
				</div>
				<div class="col-xs-3" align="center">
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('ZOOM_WIDE_START')" onmouseup="sendCmd('ZOOM_STOP')" type="button" class="btn btn-primary">Zoom In</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12"></div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('ZOOM_TELE_START')" onmouseup="sendCmd('ZOOM_STOP')" type="button" class="btn btn-primary">Zoom Out</button>
						</div>
					</div>
				</div>
				<div class="col-xs-3" align="center">
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('FOCUS_NEAR_START')" onmouseup="sendCmd('FOCUS_STOP')" type="button" class="btn btn-primary">Focus +</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('AUTO_FOCUS')" type="button" class="btn btn-primary">Focus Auto</button>
						</div>
					</div>
					<div class="row">
						<div class="col-xs-12">
							<button onmousedown="sendCmd('FOCUS_FAR_START')" onmouseup="sendCmd('FOCUS_STOP')" type="button" class="btn btn-primary">Focus -</button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<br/>
	<p style="box-shadow: 5px 5px 5px #888888;">JSON Commands</p>
	<p id="result"></p>
	
	<!-- /container -->


	<!-- Bootstrap core JavaScript
    ================================================== -->
	<!-- Placed at the end of the document so the pages load faster -->
</body>
</html>
