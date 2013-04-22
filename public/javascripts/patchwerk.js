/*jslint browser: true, devel: true */
/*global $, Door */

'use strict';

var Patchwerk = (function () {

    var Patchwerk = {};

    Patchwerk.functions = {};

    Patchwerk.functions.sendmessage = function () {
        var message, messagejson;
        message = $("#pdcommand").val();

        messagejson = '{"message": "' + message + '"}';

        $("#commanddisplay").prepend("<li>" + message + "</li>");

        $.ajax({
            url: '/sendmessage',
            type: 'POST',
            data: messagejson,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function () {
                console.log("sent message to PD");
            }
        });

        $("#pdcommand").val("");
    };

    return Patchwerk;

}());



$(document).ready(function () {

    $("#pdcommand").val("");
    $('#entercommand').click(Patchwerk.functions.sendmessage);

});

