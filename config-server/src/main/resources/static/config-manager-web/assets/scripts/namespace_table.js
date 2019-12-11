function requireFileBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#vue-require-file-detail" style="margin-right:15px;">查看详细</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        editor.txt.html(row['content'])
    },
}

$(function(){
    $('#namespace-table').bootstrapTable({
        classes: 'table table-hover',
        height: undefined,
        undefinedText: '-',
        sortName: undefined,
        sortOrder: 'asc',
        striped: true,
        editable: true,
        columns: [{
            checkbox: true
        }, {
            field: 'namespaceId',
            title: '命名空间ID',
        }, {
            field: 'namespaceName',
            title: '命名空间名称',
        }, {
            field: 'namespaceDetail',
            title: '详细',
            events: operateEvents,
            formatter: requireFileBtn,
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/demand_files',
        cache: true,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"token": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            return params;
        },
        pagination: true,
        sidePagination: 'client', // client or server
        pageNumber: 1,
        pageSize: 10,
        pageList: [10, 25, 50, 100],
        search: true,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: false,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        uniqued: 'id',
        idField: 'id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})
