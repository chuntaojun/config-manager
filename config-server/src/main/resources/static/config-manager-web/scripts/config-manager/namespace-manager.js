$(function () {
    initTableforNamespaceManager()
})

function initTableforNamespaceManager() {
    $('#namespace-manager-table').bootstrapTable({
        classes: 'table table-hover',
        height: undefined,
        undefinedText: '-',
        sortName: undefined,
        sortOrder: 'asc',
        striped: false,
        columns: [{
            checkbox: true
        },{
            field: 'id',
            title: '编号',
        },{
            field: 'namespace',
            title: '命名空间',
        },{
            field: 'namespaceId',
            title: '命名空间UUID',
        },{
            field: 'owners',
            title: '拥有权限者',
        },{
            field: 'operation',
            title: '操作',
        }],
        data: [],
        method: 'get',
        url: '/api/v1/namespace/all',
        cache: false,
        contentType: 'application/json',
        queryParams: function (params) {
            return params;
        },
        queryParamsType: 'limit', // undefined
        responseHandler: function (res) {
            return res;
        },
        pagination: true,
        sidePagination: 'server', // client or server
        totalRows: 0, // server side need to set
        pageNumber: 1,
        pageSize: 10,
        pageList: [10, 25, 50, 100],
        search: true,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: true,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        idField: 'no',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar_post',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
}