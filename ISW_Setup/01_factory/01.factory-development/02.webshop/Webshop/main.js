// Hides the topping options if the Topping checkbox is checked
function showHideToppings() {
    if (document.getElementById('toppings').checked) {
        document.getElementById('toppingsForm').style.display='block';
    } else {
        document.getElementById('toppingsForm').style.display='none';
    }
}

//Disables the Toppings checkbox if a Topping option has been selected
function freezeToppings() {
    if (document.getElementById('schokoraspeln1').checked||document.getElementById('kokosraspeln1').checked||document.getElementById('mandeln1').checked||document.getElementById('schokoraspeln2').checked||document.getElementById('kokosraspeln2').checked||document.getElementById('mandeln2').checked) {
        document.getElementById('toppings').disabled = true;
    }
    else {
        document.getElementById('toppings').disabled = false;
    }
}

var form = document.getElementById('myform');

var warenkorb = [];

// If there is an object in session storage called mywarenkorbUpd, which only exists if you return from the Shopping kart, the warenkorb JS object is filled with it
document.addEventListener("DOMContentLoaded", function() {
  if (JSON.parse(sessionStorage.getItem("mywarenkorbUpdt")) != null) {
    warenkorb = JSON.parse(sessionStorage.getItem("mywarenkorbUpdt"));
  }
});

//The add to cart button submits the form.
form.addEventListener('submit', function(event) {
  //prevent submit
  event.preventDefault();
  //create new object
  var yoghurt = {};
  //iterate over form elements to convert them to properties of the object
  for (var i = 0; i < form.elements.length-1; i++) {
    var field = form.elements[i];
    if (field.type === "radio") {
      if (field.checked) {
        yoghurt[field.name] = field.value;
      }
    } else if (field.type === "checkbox") {
      if (field.checked) {
        yoghurt[field.name]  = 'true'
      } else {
        yoghurt[field.name] = 'false';
      }
    } else {
      yoghurt[field.name] = field.value;
    }
      //delete the toppings checkbox, if there are selected toppings, they alrady are attributes
      delete yoghurt.toppings;
    }
    //add the new yoghurt object to the warenkorb array
    warenkorb.push(yoghurt);
    //reset form
    document.forms[0].reset();
    //activate the dynamic form functions again
    freezeToppings();
    showHideToppings();
    //for display purposes only
    console.warn('added', {warenkorb});
    var jString = JSON.stringify(warenkorb);
    sessionStorage.setItem('mywarenkorb', JSON.stringify(warenkorb));
});