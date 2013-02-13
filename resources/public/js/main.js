State = {};

$(document).ready(function(){
    $("#login-form>form>input[name=url]").val(window.location.pathname);

    $("#register-link").click(onRegister);

    $.ajax({
        url: "/whoami",
    }).error(function(xhr, status, error){
        //TODO: handle errors
        if (xhr.status == 401){
            $('#login-window').modal();
        }
    }).success(function(data, status, xhr){
        State.user = data.username;
        State.user_info = data;

        $(".container>h1:first").html(data['real-name']+"'s shopping list");
        buildShoppingList(State.user);
    });

    var newItemDoc = "Format:<br>";
    newItemDoc += "<ul>";
    newItemDoc += "<li>#quantity</li>";
    newItemDoc += "<li>!priority</li>";
    newItemDoc += "</ul>";
    newItemDoc += "Example: milk #1l !high";

    $("#new-item").popover({
        trigger: 'focus',
        html: true,
        title: 'Add a new item',
        content: newItemDoc});

    $("#new-item").keyup(onNewItem);
});

function buildShoppingList(user){
    $.ajax({
        url: "/lists/"+user
    }).success(function(data, status, xhr){
        State.list = data.list;
        buildUl(data.list);
    }).error(function(xhr, status, error){
        //TODO: handle errors
        if (xhr.status == 401){
            $('#login-window').modal();
        }
    });
}

function buildUl(data){
    var buildLi = function(data, id, i){
        var aId = 'item-a-'+i;
        var a = '<a tabindex="-1" href="#" class="item-text" id="'+aId+'">'+data[i].item+'</a>';
        var li = '<li id="'+id+'">'+a+'</li>';
        return $(li);
    }

    var ul = $("#shopping-list>ul");
    ul.html("");
    var id;
    for(var i=0; i<data.length; i++){
        id = 'sl-item-'+i;
        li = buildLi(data, id, i);

        am = buildActionMenu(i);

        li.find('a').first().popover({trigger: 'focus', html: true, 
            title: "What do you want to do?",
            content: am});
        ul.append(li);
    }

    $("#shopping-list").show();
}

function buildActionMenu(idx){
    var span = $('<span>').text('Choose an action for "'+State.list[idx].item+'":');
    span.append("<br>");
    var ul = $("<ul>");
    span.append(ul);

    var bought = $('<li>Set as bought</li>');
    var remove = $('<li><a href="#" data-idx="'+idx+'">Delete item</a></li>');
    remove.on('click', onDelete);

    ul.append(bought);
    ul.append(remove);

    return span;
}

function onItem(ev){
    var el = ev.target;
    $("#"+el.id).popover({title: "What do you want to do?", content: "PUPPA"});

    ev.preventDefault();
}

function onDelete(ev){
    //TODO: controllare perche' l'evento viene scatenato solo una volta
    var idx = ev.target.getAttribute('data-idx');
    r = confirm("Delete "+State.list[idx].item+"?")

    if (r){
        State.list.splice(idx, 1);

        save();
    }
}

function onNewItem(ev){
    if(ev.which == 13) {
        var text = $("#new-item").val().trim();
        var value = {item: text};
        if (!text){
            ev.preventDefault();
            return;
        }

        var found = false;
        for (i=0; i<State.list.length; i++){
            if (State.list[i].item.toLowerCase() == text.toLowerCase()){
                found = true;
                break;
            }
        }

        if (found){
            //TODO: aggiornare quantità ecc
        } else{
            State.list.push(value);
        }

        save();

        $("#new-item").val("");
        ev.preventDefault();
	}
}

function save(){
    $.ajax({
        url: "/lists/"+State.user,
        method: "PUT",
        contentType: "application/json",
        data: JSON.stringify(State.list)
    }).success(function(data, status, xhr){
        buildUl(data.list);
    }).error(function(xhr, status, error){
        //TODO: handle errors
        if (xhr.status == 401){
            $('#login-window').modal();
        }
    });
}

function onRegister(ev){
    $('#login-window').modal('hide');
    $('#register-window').modal();

    $('#register-button').click(doRegister);
}

function doRegister(){
    var form = $('#register-form>form:first');
    var obj = {};
    $.each(form.serializeArray(), function(_, kv) {
        obj[kv.name] = kv.value;
    });
    $.ajax({
        url: "/register",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(obj)
    }).success(function(data, status, xhr){
        $('#register-window').modal('hide');
        $('#confirmation-window').modal();

    }).error(function(xhr, status, error){
        alert(JSON.parse(error));
    });
}
