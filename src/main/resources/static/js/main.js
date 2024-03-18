'use strict';

var roomPage_index = document.querySelector("#room-page");
 var roomPage = document.querySelector("#roomForm");

// var createDoc = document.getElementById("create");
// var signInDoc = document.getElementById("sign-in");
var docName = document.querySelector("#docId");
var docUser = document.querySelector("#docUser");

var userSession = document.querySelector("#userSession");
// var createBtn = document.querySelector("#create");
// var signInBtn = document.querySelector("#sign-in");

var docUrl = document.querySelector("#textEditor");
var editor = document.querySelector("#edit-area");
var saveDoc = document.querySelector("#save");
var viewPriv = document.querySelector("#viewPrivilege");
var addUserForm = document.querySelector("#authentication");

var usersCon = document.querySelector("#usersConnected");
var comments = document.querySelector("#comments");

var stompClient = null;
var docId = docName.value.trim();
var user = docUser.value.trim();
var textContent = null;
var previousTextArea = null;
var whichBtn = null;


function generate() {
    //var roomId = document.getElementById("roomID");

    let alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    let symbols = "%!@#$^&*-+=|\\(){}:\"';,?";
    let password = "";

    for (let i = 0; i < 4; i++) {
        let randomNumber = Math.floor(Math.random() * 10);
        let lowerCaseLetter = alphabet.charAt(Math.random() * 26).toLowerCase();
        let upperCaseLetter = alphabet.charAt(Math.random() * 26).toUpperCase();
        //let specialChar = symbols.charAt(Math.random() * symbols.length);

        password += randomNumber + lowerCaseLetter + upperCaseLetter ;
    }
    console.log("this is room ", password);

    docName.value = password;
    console.log('this is testing',docName.value);
}



var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {

    var submitBtn = event.submitter;

    console.log("submit button event ",submitBtn.getAttribute('id'));

    if (submitBtn.value === "create") {

        whichBtn = "create";
    }
    else {
        whichBtn = "signIn";
    }

    console.log("docId given",docId);
    console.log("doc user given",docUser.value.trim());
    console.log("submit button pressed ",whichBtn);
    roomPage_index.classList.add('hidden');
    //roomPage.classList.add('hidden');

    docUrl.classList.remove('hidden');
    addUserForm.classList.remove('hidden');

    var socket = new SockJS('/ws');
    console.log("websocket established");

    stompClient = Stomp.over(socket);
    stompClient.connect({},onConnected,onError);

    event.preventDefault();

}

function onConnected() {

    console.log("try to connect to socket");


    stompClient.subscribe('/topic/public', onMessageReceived);
    if (viewPriv.checked) {

        console.log("View radio is checked");

        stompClient.send('/app/controllers.subscribeToView',
            {},
            JSON.stringify({senderId: docUser.value.trim(), type: 'JOIN', text: "", docId: docName.value.trim()})
        );
        userSessionName('viewer '+docUser.value.trim());

    } else {
        if (whichBtn === "create") {

            stompClient.send('/app/controllers.docRoomUser',
                {},
                JSON.stringify({senderId: docUser.value.trim(), type: 'JOIN', text: "", docId: docName.value.trim()})
            );

            userSessionName('owner '+docUser.value.trim());

        }
        else if (whichBtn === "signIn") {

            stompClient.send('/app/controllers.signInRoomUser',
                {},
                JSON.stringify({senderId: docUser.value.trim(), type: 'JOIN', text: "", docId: docName.value.trim()})
            );
            userSessionName('editing '+docUser.value.trim());
        }
    }

    // userSessionName(docUser.value.trim());

    findAndDisplayConnected().then();

}

function createBtnName(name) {

    var namearr = null;
    var namefinal = null;
    var nameconcat = null;


    //a function to return username without the @ else return just
    // the username
    if (name.toString().includes("@")) {

        namearr = name.toString().split("@");
        nameconcat = namearr[0].concat(namearr[1]);

        if (nameconcat.includes(".")) {
            namearr = nameconcat.split(".");
            namefinal = namearr[0].concat(namearr[1]);
        }
    }
    else {
        namefinal = name.toString();
    }

    return namefinal;
}

async function findAndDisplayConnected() {

    const connectedUserResponse = await fetch('/users');
    let connectedUsers = await connectedUserResponse.json();
    connectedUsers = connectedUsers.filter(user => user.name !== user.text);

    usersCon.innerHTML = '';

    let li = document.createElement('p');
    let textEl = document.createTextNode("Connected Users");
    li.appendChild(textEl);
    usersCon.appendChild(li);

    connectedUsers.forEach(user => {

        console.log("connected user ",user.name);

        let username = user.name;
        let nameUser = createBtnName(username);


        let li = document.createElement('li')
        // li.setAttribute('id','li'+user.name);

        li.setAttribute('id',"li"+nameUser);
        console.log("connected user html element ",li.getAttribute('id'));

        let span = document.createElement('span');
        let btn = document.createElement('button');
        btn.setAttribute('type','button');

        // btn.setAttribute('id','btn'+user.name);
        btn.setAttribute('id',nameUser);
        console.log("button user ",btn.getAttribute('id'));

        btn.classList.add('default');
        btn.style['background-color'] = getAvatarColor(user.name);

        btn.innerText = user.name[0].toUpperCase();

        btn.addEventListener('click',userDisconnection);

        let textLi = document.createTextNode(' '+user.name );

        span.appendChild(btn);
        span.appendChild(textLi);
        li.appendChild(span);
        usersCon.appendChild(li);
    });

}

function userDisconnection(event) {


    var clickedUser = event.target;
    var selectedUser = clickedUser.getAttribute('id');

    console.log("button ",selectedUser);

    //var deletedElement = document.querySelector("#li"+selectedUser);

    var deletedElement = document.querySelector("#li"+selectedUser);
    console.log("selected User to delete button",selectedUser);
    // console.log("li to deleted",document.querySelector("#li"+selectedUser).getAttribute('id'));

    console.log("li to deleted ",deletedElement.getAttribute('id'));
    usersCon.removeChild(deletedElement);

    var msg = {
        senderId: docUser.value.trim(),
        docId: docName.value.trim(),
        text: editor.value,
        type: 'LEAVE'
    }

// try to make disconnect work
// this is to send to controller to update database and send
//  a DISCONNECT message to all
    stompClient.send('/app/controllers.onDisconnect',
        {},
        JSON.stringify(msg));

    // this is for js client to disconnect
    if (stompClient !== null) {
        stompClient.disconnect(function() {
            console.log("user disconnected");
        });
        stompClient = null;
    }
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function userSessionName(name) {

    console.log("try to write user session");

    let userSessionN = document.createElement('i')

    let userSessionText = document.createTextNode(' ' + name);
    userSessionN.appendChild(userSessionText);
    userSession.appendChild(userSessionN);

}


function onError() {
    connectingElement.textContent('Could not connect to socket! Refresh and try...');
    connectingElement.style['color'] = 'red';
}


function writeToEditor(event) {

    textContent = editor.value;

    if (textContent && stompClient) {
        var editorMessage = {
            senderId: docUser.value.trim(),
            docId: docName.value.trim(),
            text: textContent,
            type: 'TEXT'
        }

        if (!viewPriv.checked) {

        stompClient.send('/app/controllers.sendMessage', {},
            JSON.stringify(editorMessage));
        }

    }
    event.preventDefault();
}


async function onMessageReceived(payload) {

    var textElement = null;
    var messageText = null;
    var text = null;


    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    await findAndDisplayConnected().then();

    editor.value = message.text;
    //editor.value = previousTextArea;
    editor.scrollTop = editor.scrollHeight;

}

async function addShareUser(event,form) {

    event.preventDefault();

    let btnSubmit = document.querySelector("#btnSubmit");
    btnSubmit.disabled = false;

    let data = document.querySelector("#username");
    console.log("data form: user to add ",data.value.trim());
    console.log("data form: user session ",docUser.value.trim());
    console.log("data form: docId ",docName.value.trim());

    let viewUser = document.querySelector("#viewUser");
    let editUser = document.querySelector("#editUser");
    var role = null;

    if (viewUser.checked) {
        console.log("view User checked");
        role = 'VIEW';
    }
    if (editUser.checked) {

        role = "EDIT";
        console.log("edit User checked",role);
    }

    console.log("data form: role ",role);

    let ownerUser = docUser.value.trim();
    let docIdName = docName.value.trim();
    let userResultJson = await fetch(`http://localhost:8090/editor/addShareUser/${docIdName}/${ownerUser}`, {
            method: 'POST',
            body: JSON.stringify({name: data.value.trim(),
                                        status: "JOIN",
                                        role: role
                                        }),
            headers:{
                'Content-Type': 'application/json'
            }
        });

    let userResult = await userResultJson.json();
    console.log(userResult);
    if (!userResult) {
        data.value = '';
        viewUser.checked = false;
        editUser.checked = false;
        alert("You don't have rights to add user to document! You are NOT the owner");
    }
    else {
        data.value = '';
        viewUser.checked = false;
        editUser.checked = false;
        alert('user '+data.value.trim()+' added')
    }
}

 async function saveDocument(event) {

    var clicked = event.target;
    var clickedBtn = clicked.getAttribute('id');

    console.log("name of clicked button",clickedBtn);
    const response =  await fetch('/save/'+docName.value.trim() + '/' + docUser.value.trim());
    let replyText =await  response.text();

    if ("200" === replyText) {
        console.log("doc saved");
        alert("document saved locally!")
    }
    else { console.log("errorrr!!!");
            alert("cannot save file...")}

}

roomPage.addEventListener('submit',connect,true);
addUserForm.addEventListener('submit',addShareUser,true);


editor.addEventListener('input',writeToEditor,true);
saveDoc.addEventListener('click',saveDocument,true);

