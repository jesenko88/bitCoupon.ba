(function($) {$(document).ready(function() {                
    //--- Images slider on Offer page
    if($('#imagegal').length != 0)
    {
        $('#imagegal').royalSlider({
            fullscreen: {
                enabled: false,
                nativeFS: true
            },
            globalCaption:true,
            imageScaleMode: 'fit-if-smaller',
            imageScalePadding: 6,
            imageAlignCenter:true,
            controlNavigation: 'thumbnails',
            thumbs: {
                orientation: 'vertical',
                paddingBottom: 4,
                appendSpan: true,
                spacing:8
            },
            transitionType: 'fade',
            autoScaleSlider: true, 
            autoScaleSliderWidth: 960, 
            autoScaleSliderHeight: 530,
            loop: false,
            numImagesToPreload:3,
            arrowsNavAutohide: true,
            arrowsNavHideOnTouch: true,
            keyboardNavEnabled: true,
        });
    }

    if($('#top3slider').length != 0)
    {
        $('#top3slider').royalSlider({
            arrowsNav: true,
            loop: true,
            loopRewind: true,
            sliderDrag: true,
            arrowsNavAutohide: true,
            arrowsNavHideOnTouch: true,
            keyboardNavEnabled: true,
            controlNavigation: 'none',
            autoPlay: {
                enabled: true
            }
            }
        );
    }

    //--- FAQ
    $('.faq h4').click(function(e) {
        e.preventDefault();
        $(this).toggleClass('active');
        $(this).next('div.ans').slideToggle(500);
    });

    //--- More Info slide-down action
    $('.btn-moreinfo').off().click(function(e) {            
        e.preventDefault();            
        $(this).next('.collapsed').slideToggle(700);        
    });

    //--- Tabovi
    if ($('ul.tabs').length != 0){            
        $('ul.tabs').idTabs();        
    }     

    //--- Fancybox
    if ($('a.fancy').length != 0){            
        $('a.fancy').fancybox();        
    }    

    //--- Support slide-down widget
    setTimeout(function(){
        $('.support').slideDown(800);
        $('.support .btn-close').click(function(e) {
            e.preventDefault();
            $('.support').slideUp(400);
        });
        }, 2000)

    // CHECKOUT        
    $('#select_offer_count').change(function(e) { 
        var new_price_calculate = (parseFloat($("#offer_price_now").text()) * parseFloat($(this).val())).toFixed(2);
        $("#offer_price_calculated").text(new_price_calculate);   
        $("#checkout_ekredit").val(new_price_calculate);   

        var name_lastname = $('#checkout1-form').data('uip');
        $("#checkout1-form-names").empty();
        for(var i = 1; i <= $(this).val(); i++) {
            if(i > 1){name_lastname = ''};
            $("#checkout1-form-names").append(
                '<div class="checkout1_item row-fluid"><div class="span1"><label for="checkout1_item'+i+'"><span class="numero">'+i+'</span></label></div>' +
                '<div class="span14"><input type="text" value="'+name_lastname+'" name="checkout1_names[]" placeholder="Ime i prezime" class="form-block"></div></div>'
            );
        }
    });


    // Step 1
    $('#checkout1-submit').click(function(e) { 
        e.preventDefault();
        // AJAX Submit     
        var url_ajax = $('#checkout1-form').attr('action');
        var form_msg = $('#checkout1-form-msg');

        $.ajax({
            url: url_ajax + '/1', 
            type: 'POST',
            data: $('#checkout1-form').serializeArray(),
            dataType: "html",
            cache: false,
            success: function (response){
                try {
                    var response = jQuery.parseJSON(response);
                } catch(err){

                }
                if(response.status == '2'){
                    window.location.href = "'"+response.data+"'";
                }else{
                    if(response.status == '0'){
                        $(form_msg).html(response.data).removeClass().addClass('ajax-form-msg alert alert-error');
                    }else{
                        $('#checkout1-form').submit();
                    }
                }
            },
            error: function (response,status){
            }

        });
    });



    // Nletter
    $('#nletter-form-submit').click(function(e) { 
        e.preventDefault();
        // AJAX Submit     
        var now = new Date();        
        var url_ajax = $('#nletter-form').attr('action') + '/' + now.valueOf().toString();
        var form_msg = $('#nletter-form-msg');

        $.ajax({
            url: url_ajax, 
            type: 'POST',
            data: $('#nletter-form').serializeArray(),
            dataType: "json",
            cache: false,
            success: function (response){
                if(response.status == '0'){
                    $(form_msg).html(response.data);
                }else{
                    $(form_msg).html(response.data);
                }
            },
            error: function (response,status){
            }

        });
    });


    $('#pikpay_dugme form').submit(function(e) { 
        if($('#checkout2-form-accept').prop("checked") == false){
            alert("Za nastavak, morate prihvatiti \"Pravila korištenja servisa\"");
            return false;                
        }
        return true;
    });


    // Ajax Modal
    $('.openAjaxModal').off().click(function (e) {
        e.preventDefault();
        var url = $(this).attr('href');
        var is_multi = $(this).data('multi');
        $.get(url, function(data) {
            if(is_multi == '1'){
                $('<div id="multiOffer" class="modal hide fade">' + data + '</div>').modal().on('hidden', function () {
                    $(this).remove();
                })
            }else{
                $('<div class="modal hide fade">' + data + '</div>').modal().on('hidden', function () {
                    $(this).remove();
                })
            }

        }).success(function() {
            $('input:text:visible:first').focus(); 
        });
    });

    });
})(jQuery);

function acceptCheckout(){
    if($('#checkout2-form-accept').prop("checked") == false){
        alert("Za nastavak, morate prihvatiti \"Pravila korištenja servisa\"");
        return false;                
    }
    return true;
}