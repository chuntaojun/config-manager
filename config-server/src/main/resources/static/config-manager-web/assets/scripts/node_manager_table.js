function storageBtn(value, row, index) {
    return [
        '<a class="LookDetail btn btn-info" href="' + row['remote'] + '" style="margin-right:15px;">对应控制台</a>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
    },
}

$(function () {
    $('#node_table').bootstrapTable({
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
            field: 'id',
            title: '节点编号',
        }, {
            field: 'ip',
            title: '节点IP',
        }, {
            field: 'status',
            title: '节点状态'
        }, {
            field: 'role',
            title: '角色',
        }, {
            field: 'remote',
            title: '对应控制台',
            events: operateEvents,
            formatter: storageBtn
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/v1/cluster/all',
        cache: true,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"config-manager-token": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            return params;
        },
        pagination: true,
        sidePagination: 'client', // client or server
        pageNumber: 1,
        pageSize: 10,
        pageList: [10, 25],
        search: true,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: false,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        uniqued: 'report_id',
        idField: 'report_id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar_seller',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})