if(document.readyState == 'loading') {
    document.addEventListener('DOMContentLoaded', ready)
} else{
    ready()
}

var message = '{ }'

function ready(){
    var removeCartItemButtons = document.getElementsByClassName('btn-delete')
    for(var i=0; i<removeCartItemButtons.length; i++){
        var button = removeCartItemButtons[i]
        button.addEventListener('click', removeCartItem)
    }

    var quantityInputs = document.getElementsByClassName('cart-anzahl-input')
    for (var i=0; i<quantityInputs.length; i++){
        var input = quantityInputs[i]
        input.addEventListener('change', quantityChanged)
    }

    var addToCartButton = document.getElementsByClassName('btn-checkout')
    addToCartButton[0].addEventListener('click', addToCartClicked)

    document.getElementsByClassName('btn-purchase')[0].addEventListener('click', purchaseClicked)

    var randomButton = document.getElementsByClassName('btn-random')
    randomButton[0].addEventListener('click', randomButtonClicked)
}

function getRandomInt(max){
    return Math.floor(Math.random()*max)
}

function randomButtonClicked(event){
    var button = event.target
    var configuration = button.parentElement
    var geschmacksElements = configuration.getElementsByClassName("Geschmack")
    var toppingElements = configuration.getElementsByClassName("Topping")
    var frozenElements = configuration.getElementsByClassName("Frozen")
    geschmacksElements[getRandomInt(geschmacksElements.length)].checked = true;
    for(var i=0; i<toppingElements.length; i++){
        if(getRandomInt(2) == 0){
            toppingElements[i].checked = true;
        }else{
            toppingElements[i].checked = false;
        }
    }
    frozenElements[getRandomInt(frozenElements.length)].checked = true;
}

function purchaseClicked(){
    makeJson()
    sendMessage()

    var cartItems = document.getElementsByClassName('cart-items')[0]
    while(cartItems.hasChildNodes()){
        cartItems.removeChild(cartItems.firstChild)
    }
    updateCartTotal()
}

function sendMessage(){

    if(message !== null){
        fetch("/publish", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({msg: message})
        })
        .then(res => res.json())
        .then(data => {
            if(data.success) {
                alert("Bestellung versendet!")
            }
        })
    }

    testVar = '{ }'
}

function makeJson(){
    var cartItems = document.getElementsByClassName('cart-items')[0]

    message = '{"rawOrder":['

    for(var i=0; i<cartItems.getElementsByClassName("cart-row").length; i++)
    {
        message += '{"flavour":' + '"' + cartItems.getElementsByClassName("cart-row")[i].getElementsByClassName("cart-geschmack cart-column")[0].getHTML() + '",'
        message += '"toppings":["'
        
        var originalString = cartItems.getElementsByClassName("cart-row")[i].getElementsByClassName("cart-topping cart-column")[0].getHTML()

        for(var j=0; j<originalString.length; j++)
        {
            if(originalString[j] == ',')
            {
                message += '","'
            }
            else
            {
                message += originalString[j]
            }
        }

        message += '"],"frozen":'

        tempFrozen = cartItems.getElementsByClassName("cart-row")[i].getElementsByClassName("cart-frozen cart-column")[0].getHTML()

        if(tempFrozen == "Ja")
        {
            message += 'true},'
        }
        else
        {
            message += 'false},'
        }
        
    }

    message += ']}'
}

function removeCartItem(event){
    var buttonClicked = event.target
    buttonClicked.parentElement.parentElement.remove()
    updateCartTotal()
}

function quantityChanged(event){
    var input = event.target
    if(isNaN(input.value) || input.value<=0){
        input.value = 1
    }
    updateCartTotal()
}

function addToCartClicked(event){
    var button = event.target
    var configuration = button.parentElement
    var geschmack = configuration.querySelector('input[name="Geschmack"]:checked').value
    var toppings = []
    var toppingElements = configuration.getElementsByClassName("Topping")
    for(var i=0; i<toppingElements.length; i++){
        if(toppingElements[i].checked){
                toppings.push(toppingElements[i].value)
        }
    }
    var frozen = configuration.querySelector('input[name="Frozen"]:checked').value
    var price = 3.00
    if(toppings.length >0){
        price = price + (0.50 * toppings.length)
    }
    if(frozen == "Ja"){
        price = price+ 0.50
    }
    var priceText = String(price.toFixed(2)).replace('.', ',') + '€'
    addItemToCart(geschmack, toppings, frozen, priceText)
    updateCartTotal()
}

function addItemToCart(geschmack, toppings, frozen, priceText){
    var cartRow = document.createElement('div')
    cartRow.classList.add('cart-row')
    var cartItems = document.getElementsByClassName('cart-items')[0]
    var cartItemGeschmäcker = cartItems.getElementsByClassName("cart-geschmack")
    var cartItemToppings = cartItems.getElementsByClassName("cart-topping")
    var cartItemFrozen = cartItems.getElementsByClassName("cart-frozen")
    for(var i=0; i<cartItemGeschmäcker.length; i++){
        if(cartItemGeschmäcker[i].innerText==geschmack && cartItemToppings[i].innerText==toppings && cartItemFrozen[i].innerText==frozen){
            alert('Diese Auswahl ist bereits im Warenkorb')
            return
        }
    }
    var cartRowContents = `
        <span class="cart-geschmack cart-column">${geschmack}</span>
        <span class="cart-topping cart-column">${toppings}</span>
        <span class="cart-frozen cart-column">${frozen}</span>
        <span class="cart-preis cart-column">${priceText}</span>
        <div class="cart-anzahl cart-column">
            <input class="cart-anzahl-input" type="number" value="1">
            <button class="btn btn-delete cart-anzahl-button" role="button">Löschen</button>
        </div>`
    cartRow.innerHTML = cartRowContents
    cartItems.append(cartRow)
    cartRow.getElementsByClassName('btn-delete')[0].addEventListener('click', removeCartItem)
    cartRow.getElementsByClassName('cart-anzahl-input')[0].addEventListener('change', quantityChanged)
}


function updateCartTotal() {
    var cartItemContainer = document.getElementsByClassName('cart-items')[0]
    var cartRows = cartItemContainer.getElementsByClassName('cart-row')
    var total = 0.00
    for(var i = 0; i < cartRows.length; i++){
        var cartRow = cartRows[i]
        var priceElement = cartRow.getElementsByClassName('cart-preis')[0]
        var quantityElement = cartRow.getElementsByClassName('cart-anzahl-input')[0]
        var price = parseFloat(priceElement.innerText.replace('€', '').replace(',', '.'))
        var quantity = quantityElement.value
        total = total + (price*quantity)
    }
    total = Math.round(total * 100) / 100
    document.getElementsByClassName('cart-summe-preis')[0].innerText = total.toFixed(2) + '€'
    document.getElementsByClassName('cart-summe-preis')[0].innerText = document.getElementsByClassName('cart-summe-preis')[0].innerText.replace('.', ',')
}