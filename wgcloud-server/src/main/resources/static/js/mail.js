function view(id) {
    window.location.href = "/lins/appInfo/view?id=" + id;
}

function del(id) {
    window.location.href = "/lins/appInfo/del?id=" + id;
}

function cancel() {
    history.back();
}