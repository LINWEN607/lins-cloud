function searchByPara() {
    var account = $("#account").val();
    window.location.href = "/lins/log/list?account=" + escape(escape(account));
}

function view(id) {
    window.location.href = "/lins/log/view?id=" + id;
}

function cancel() {
    history.back();
}
