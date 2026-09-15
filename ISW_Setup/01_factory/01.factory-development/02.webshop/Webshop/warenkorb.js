// Server to be defined in the end

//maven priject and java imports never worked out for me, maybe for you
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;

//shows message "Dein Warenkorb ist leider leer :("
function ShowEmptyWarenkorb(){
    document.getElementById("empty").style.display = "inline";
}

//Room for a future picture + object attributes that will be displayed in the shopping kart table + Column where the remove button comes
var columns = ['Bild','basis','frozen', 'topping1', 'topping2','anzahl','Aktion'];


//function to build the table. List is the list array, columns were previously defined, container is where the table will be shown
function buildTable(list,columns,container) {
    var table = document.createElement('table');
    table.setAttribute("id", "tableid")
    var headers = document.createElement("tr");
    //iterate over columns
    for (var i = 0; i < columns.length; i++) {
        var header = document.createElement("th");
        header.innerText = columns[i];
        headers.appendChild(header);
    }
    table.appendChild(headers);
    // Iterate over the list of objects
    for (var i = 0; i < list.length; i++) {
        var obj = list[i];
        // Create a table row for each object
        var row = document.createElement("tr");
    
        // Iterate over the columns array to create the table cells
        for (var j = 0; j < columns.length; j++) {
            // Get the property name
            var key = columns[j];
            // Use a default value if the property is missing
            var value = obj[key] || "-";
            // Create a table cell for the property
            var cell = document.createElement("td");
            cell.innerText = value;
            row.appendChild(cell);
        }
    
        // Add the row to the table
        table.appendChild(row);
    }
    container.appendChild(table);

    // Add the remove button
    var rows = table.getElementsByTagName("tr");
    for (var i = 1; i < rows.length; i++) {
        var cell = rows[i].cells[6];
        var button = document.createElement("button");
        button.innerHTML = "Remove";
        button.setAttribute("class", "allRemoveButtons");
        button.setAttribute("data-index", i-1);
        button.addEventListener("click", function() {
            var index = this.getAttribute("data-index");
            list.splice(index, 1);
            var updatedJsonString = JSON.stringify(list);
            sessionStorage.setItem("mywarenkorbUpdt", updatedJsonString);
            sessionStorage.setItem("mywarenkorb", updatedJsonString);
            this.parentNode.parentNode.remove();
            //update the data-index attribute of the remove buttons to match the new updated list
            for (var j = 1; j < rows.length; j++) {
                var jthRow = table.getElementsByTagName("tr")[j];
                var seventhCell = jthRow.getElementsByTagName("td")[6];
                var removebutton = seventhCell.firstElementChild;
                removebutton.setAttribute("data-index", j-1);
            }
            if (list.length===0) {
                document.getElementById("empty").style.display = "inline";
                document.getElementById("container").style.display = "none";
                document.getElementById("sendBtn").disabled = true;
            }
        });
        //Deletes the default '-' shown in the Aktion column
    cell.innerHTML = "";
    cell.appendChild(button);
    }
}

var container = document.getElementById("container");


//Runs when the page is loaded
document.addEventListener("DOMContentLoaded", function() {
// Read the JSON list object from session storage
var jsonString = sessionStorage.getItem("mywarenkorb");
sessionStorage.setItem("mywarenkorbUpdt", jsonString);

// Check if the JSON string is not null
if (jsonString != null) {
    // Parse the JSON string into a JavaScript object
    var list = JSON.parse(jsonString);

    // Create the table
    buildTable(list, columns, container);
} else {
    ShowEmptyWarenkorb();
}
});

// define the new order button, cleans all session storage and returns to index
var newOrderBtn = document.getElementById("newOrderBtn");
newOrderBtn.addEventListener('click', function(){
    sessionStorage.clear();
})



//Before making the POST request, a timestamp is added at the end of the order object
var sendButton = document.getElementById('sendBtn');
sendButton.addEventListener('click', function(){
    let myOrder = JSON.parse(sessionStorage.getItem("mywarenkorb"));
    let timestamp = {timestamp:new Date()};
    myOrder.push(timestamp);
    sessionStorage.setItem("outcomingOrder", JSON.stringify(myOrder));
    document.getElementById("bestellungErfolg").style.display = "";
    var allRemoveButtons = document.getElementsByClassName('allRemoveButtons');
    for (var i=0;i<allRemoveButtons.length;i+=1){
        allRemoveButtons[i].disabled = true;
    }
    document.getElementById("sendBtn").style.display = "none";
    document.getElementById("weiterMixenBtn").style.display = "none";
    document.getElementById("newOrderBtn").style.display = "";

    // Define the server
    fetch('/server', {
        method: 'POST',
        body: JSON.stringify(myOrder),
        headers: {
          'Content-Type': 'application/json'
        }
      })
})