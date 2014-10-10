var selectedElement = 0;
var currentX = 0;
var currentY = 0;
var currentMatrix = 0;

function selectElement(evt) {
  selectedElement = evt.target;

  currentX = evt.clientX;
  currentY = evt.clientY;
  currentMatrix = selectedElement.parentNode.getAttributeNS(null, "transform").slice(7,-1).split(',');

  for(var i=0; i<currentMatrix.length; i++) {
    currentMatrix[i] = parseFloat(currentMatrix[i]);
  }
  
  selectedElement.setAttributeNS(null, "onmousemove", "moveElement(evt)");
  selectedElement.setAttributeNS(null, "onmouseout", "deselectElement(evt)");
  selectedElement.setAttributeNS(null, "onmouseup", "deselectElement(evt)");
}
    
function moveElement(evt) {
  var dx = evt.clientX - currentX;
  var dy = evt.clientY - currentY;
  currentMatrix[4] += dx;
  currentMatrix[5] += dy;
  
  selectedElement.parentNode.setAttributeNS(null, "transform", "matrix(" + currentMatrix.join(',') + ")");
  currentX = evt.clientX;
  currentY = evt.clientY;
}
    
function deselectElement(evt) {
  if (selectedElement != 0) {
      selectedElement.removeAttributeNS(null, "onmousemove");
      selectedElement.removeAttributeNS(null, "onmouseout");
      selectedElement.removeAttributeNS(null, "onmouseup");
      selectedElement = 0;
  }
}
    
var contextMenuTimeout;
function showContextMenu(evt, el) {
    evt.preventDefault();
	var dv = document.getElementById("custom-context-menu");

	if (dv.className.indexOf("hide") != -1) {
	    var str = el.getElementsByTagName("text")[0].textContent;
		dv.innerHTML = "delete '" + str + "'";
		dv.setAttribute('data-g-id', el.id);
		dv.className = dv.className.replace(" hide", "");
		dv.style.left = (event.pageX-30) + "px";
		dv.style.top = (event.pageY-15) + "px";

		contextMenuTimeout = setTimeout(hideContextMenu, 3000);
	}
}
    
function hideContextMenu() {
	var dv = document.getElementById("custom-context-menu");
	if (dv.className.indexOf("hide") == -1) {
		dv.className += " hide";
		clearTimeout(contextMenuTimeout);
	}
}
    
function removeWord(dv) {
	var id = dv.getAttribute('data-g-id');
	var g = document.getElementById(id);
	g.parentNode.removeChild(g);
	hideContextMenu();
}
