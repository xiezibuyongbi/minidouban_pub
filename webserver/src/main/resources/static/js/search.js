//# sourceURL=search.js

function addBookToList() {
    const selectedListName = $("#reading-lists").val();
    if (selectedListName === "" || typeof (selectedListName) === undefined) {
        alert("请选择书单！")
        return false;
    }
    $("#selected-list-name").val(selectedListName);
    $.ajax({
        url: "/add-book?from=1",
        type: "POST",
        data: $("#add-book-to-list").serialize(),
    });
    return true;
}

function pageRequest(direction) {
    const keyword = $("#keyword").val();
    let pageNum = getQueryVar("pageNum");
    const backupPageNum = pageNum;
    pageNum = turnPage(pageNum, direction);
    $.ajax({
        url: "/search?pageNum=" + pageNum + "&keyword=" + keyword,
        type: "POST",
        success: (data) => {
            if (isPageEmpty()) {
                pageNum = backupPageNum;
                alert(noSearchResultPrompt);
            } else {
                $("#show-books").html(data);
            }
            history.pushState(null, null, "?pageNum=" + pageNum + "&keyword=" + keyword);
        }
    });
}
