function collapse_menus() {
  // collapse the non-current, non-header menu items
  $(".wy-menu-vertical ul li a").addClass("nav_collapse");
  $(".wy-menu ul.current li a").removeClass("nav_collapse");
  $(".wy-menu ul li a:has(p)").removeClass("nav_collapse");
  $(".wy-menu ul:first li a").removeClass("nav_collapse");
  
  // add _blank target to external links in the menu
  $(".wy-menu a[href^=http]").attr("target", "_blank")
  
  // make the collapsible menus clickable
  $(".wy-menu p").each(function() {
    // get the text of the span
    var text = this.firstChild.textContent
    // get the next ul>li>a.href element
    var href = this.nextElementSibling.firstElementChild.firstElementChild.getAttribute("href");
    // delete the span and replace by a
    this.removeChild(this.firstElementChild);
    var node = document.createElement("a");
    var textNode = document.createTextNode(text);
    var span = document.createElement("span");
    node.appendChild(span);
    span.appendChild(textNode);
    span.setAttribute("class", "caption-text");
    node.setAttribute("href", href);
    this.appendChild(node);
  });
}

window.onload = function() { collapse_menus(); };
