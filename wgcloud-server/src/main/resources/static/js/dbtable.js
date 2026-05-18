function view(id) {
    window.location.href = "/lins/dbTable/edit?id=" + id;
}


function add() {
    window.location.href = "/lins/dbTable/edit";
}

function del(id) {
    if (confirm('你确定要删除吗？')) {
        window.location.href = "/lins/dbTable/del?id=" + id;
    }
}

function viewChart(id) {
    window.location.href = "/lins/dbTable/viewChart?id=" + id;
}

function cancel() {
    history.back();
}
