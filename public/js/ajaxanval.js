(function($) {
$(document).ready(function() {
    // Validation
    $('.valid-latter').keyup(function(e) {
        regexProvjera(/^[a-žA-Ž ]+$/,$(this),"Dozvoljen unos samo slova");
    });

    $('.valid-brojevi').keyup(function(e) {
        regexProvjera(/^[0-9]+$/,$(this),"Dozvoljen unos samo brojeva");
    });

    $('.valid-email').keyup(function(e) {
        $('.valid-potvrda[data-poid="'+$(this).attr('id')+'"]').val('');
        var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        //regexProvjera(re,$(this),"Email adresa nije validna");
        regexProvjera(re,$(this),"");
    });

    $('.valid-tel').keyup(function(e) {
        regexProvjera(/^[0-9+ ]+$/,$(this),"Broj telefona nije validan");
    });

    $('.valid-password').keyup(function(e) {
        $('.valid-potvrda[data-poid="'+$(this).attr('id')+'"]').val('');
        if (/^[a-žA-Ž0-9]+$/.test($(this).val()) ){
            $(this).next('.help-inline').text("Šifra mora imati barem 1 specijalni znak").closest('.control-group').removeClass('success').addClass('error');
        }else{
            $(this).next('.help-inline').text("").closest('.control-group').removeClass('error').addClass('success');
        }
    });

    $('.valid-potvrda').keyup(function(e) {
        var org_input = $('#'+$(this).data('poid'));
        if ($(this).val() == org_input.val() ){
            org_input.closest('.control-group').removeClass('error').addClass('success');
            $(this).next('.help-inline').text("").closest('.control-group').removeClass('error').addClass('success');
        }else{
            org_input.closest('.control-group').removeClass('success').addClass('error');
            $(this).next('.help-inline').text("Polja nemaju iste vrijednosti").closest('.control-group').removeClass('success').addClass('error');
        }
    });


    $('.valid-standard').keyup(function(e) {
        if($(this).val() == ''){ 
            $(this).closest('.control-group').removeClass('success').removeClass('error');
            return false;
        }
        if ($(this).val() != '' ){
            $(this).next('.help-inline').text("").closest('.control-group').removeClass('error').addClass('success');
        }else{
            $(this).next('.help-inline').text("Unesite vrijednost").closest('.control-group').removeClass('success').addClass('error');
        }
    });

    function regexProvjera(provjera,element,poruka){
        if(element.val() == ''){ 
            element.next('.help-inline').text("").closest('.control-group').removeClass('success').removeClass('error');
            return false;
        }

        if (provjera.test(element.val())){
            element.next('.help-inline').text("").closest('.control-group').removeClass('error').addClass('success');
        }else{
            element.next('.help-inline').text(poruka).closest('.control-group').removeClass('success').addClass('error');
        }
    }
    
    // Recaptcha 
    $('.input-recaptcha').keyup(function(e) {
        var vrijednosti = $(this).prev('.recaptcha-calc').text().split(" + ");
        vrijednosti = parseInt(vrijednosti[0]) + parseInt(vrijednosti[1]);

        if(parseInt($(this).val()) == vrijednosti){
            $(this).next('.help-inline').text("").closest('.control-group').removeClass('error').addClass('success');
        }else{
            $(this).next('.help-inline').text("Odgovor nije tacan").closest('.control-group').removeClass('success').addClass('error');
        }
    });
    
    function validate_empty(form,form_msg,close_form_msg,scroll){
        var is_form_valid = true;
        
        $(form).find('.input').each(function() {
            if(!$(this).val()){
                is_form_valid = false;
            }
        });
        $(form).find('.control-group').each(function() {
            if($(this).hasClass('error')){
                is_form_valid = false;
            }
        });
        if(!is_form_valid){
            $(form_msg).html("Unesite ispravne vrijednosti u sva polja!").prepend(close_form_msg).addClass('alert');
            ajax_form_msg_close(form_msg);
            ajax_form_scroll_to_msg(scroll, form_msg);
            return false;
        }
        return true;
    }
    
    function ajax_form_msg_close(form_msg){
        $('.ajax-form-msg-close').off().click(function(){ $(form_msg).empty().removeClass().addClass('ajax-form-msg'); });
        if($('#ajaxProfilRespons').length != 0){ $('#ajaxProfilRespons').css('height','auto'); }
    }
    
    function ajax_form_scroll_to_msg(scroll, form_msg){
      
        if(scroll=='1'){
            $('html, body').animate({
               scrollTop: $(form_msg).offset().top - 10
           }, 100);
       }
    }
    
     
    
    
    $('.submitEnter').keypress(function(e) {
        if(e.which == 13) {
            $('button[data-formid="'+$(this).closest('form').attr('id')+'"]').click();
        }
    });
     
    // Submit AJAX Form
    $('.btn-ajax-submit').off().click(function(e){
        e.preventDefault();
       
        
        var submit_btn = $(this);
        var submit_btn_text = $(this).text();
        var form = $('#' + $(this).data('formid'));
        var form_msg = $('#' + $(this).data('formid') + '-msg');
        var close_form_msg = '<button type="button" class="ajax-form-msg-close">&times;</button>';
        var scroll = $(this).data('scroll');
       
        // Validation
        if($(this).data('input')=='1'){
            if(!validate_empty(form,form_msg,close_form_msg,scroll)){
                return false;
            }
        }
      
        if($(this).data('agree')=='1'){
            if($(form).find('.reg_agreement').prop("checked") == false){
                $(form_msg).html("Za nastavak registracije, morate prihvatiti \"Pravila korištenja servisa\"").prepend(close_form_msg).addClass('alert');
                ajax_form_msg_close(form_msg);
                ajax_form_scroll_to_msg(scroll, form_msg);
                return false;
            }
        }
        // End Validation 
        
       
        
        $(form_msg).empty().removeClass('alert');

        // AJAX Form submit
        var now = new Date();        
        var url_ajax = $(form).attr('action') + '/' + now.valueOf().toString();

        $(submit_btn).text('LOADING');
        
        $.ajax({
            url: url_ajax, 
            type: 'POST',
            dataType: "json",
            data: $(form).serialize(),
            cache: false,
            success: function (response){
                    $(submit_btn).text(submit_btn_text);
                    if(response.status == '1'){
                        $(form_msg).html(response.data).prepend(close_form_msg).removeClass().addClass('ajax-form-msg alert alert-success');                        
                        ajax_form_msg_close(form_msg);
                        ajax_form_scroll_to_msg(scroll, form_msg);
                        if(response.redirect){ 
                            setTimeout(function() {
                                window.location.replace(response.redirect);
                            }, 1000);
                        }
                    }else{
                        $(form_msg).html(response.data).prepend(close_form_msg).removeClass().addClass('ajax-form-msg alert alert-error');
                        ajax_form_msg_close(form_msg);
                        ajax_form_scroll_to_msg(scroll, form_msg);                        
                    }
                    
                    
            }
        });
    });	
    
    
});
})(jQuery);

