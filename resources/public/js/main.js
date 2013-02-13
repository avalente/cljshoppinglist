State = {};

$(document).ready(function(){
    $("#login-form>form>input[name=url]").val(window.location.pathname);
    $.ajax({
        url: "/whoami",
    }).error(function(xhr, status, error){
        if (xhr.status == 401){
            $("#login-form").css("display", "block");
        }
    }).success(function(data, status, xhr){
        State.user = data;
        buildShoppingList(data);
    });

    $("#new-item").keyup(onNewItem);
});

function buildShoppingList(user){
    $.ajax({
        url: "/lists/"+user
    }).success(function(data, status, xhr){
        State.list = data.list;
        var ul = $("#shopping-list>ul");
        var li = [];
        var id;
        for(var i=0; i<data.list.length; i++){
            id = 'sl-item-'+i;
            li.push('<li id="'+id+'"><a tabindex="-1" href="#">'+data.list[i].item+'</li>');
        }
        ul.html(li);

        for (var i=0; i<data.list.length; i++){
            $("#"+id+">a").click(onItem);
        }

        $("#shopping-list").css("display", "block");
    });
}

function onItem(){
    console.log(arguments);
}

function onNewItem(ev){
    if(ev.keyCode == 13) {
        State.list.push({item: $("#new-item").val()});

        $.ajax({
            url: "/lists/"+State.user,
            method: "PUT",
            contentType: "application/json",
            data: JSON.stringify(State.list)
        });
	}
}
