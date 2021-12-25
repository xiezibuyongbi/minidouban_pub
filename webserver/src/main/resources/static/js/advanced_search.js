//# sourceURL=advanced_search.js

function postPredicate(direction) {
    let pageNum = getQueryVar("pageNum");
    const backupPageNum = pageNum;
    pageNum = turnPage(pageNum, direction);
    $.ajax({
        url: "/advanced_search?pageNum=" + pageNum,
        contentType: "application/json; charset=UTF-8",
        type: "POST",
        data: formToJSON($("#predicate")),
        success: (data) => {
            if (isPageEmpty()) {
                pageNum = backupPageNum;
                alert(noSearchResultPrompt);
            } else {
                $("#show-books").html(data);
            }
            history.pushState(null, null, "?pageNum=" + pageNum);
        }
    })
}