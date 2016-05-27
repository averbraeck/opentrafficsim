$(function(){
  var includes = $('.include');
  jQuery.each(includes, function(){
    var file = $(this).data('include');
    $(this).load(file);
  });
});
