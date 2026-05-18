function add() {
    window.location.href = "/lins/containerInfo/edit";
}

function edit(id) {
    window.location.href = "/lins/containerInfo/edit?id=" + id;
}

function del(id) {
    if (confirm('你确定要删除吗？')) {
        window.location.href = "/lins/containerInfo/del?id=" + id;
    }
}

function goback() {
    history.back();
}
