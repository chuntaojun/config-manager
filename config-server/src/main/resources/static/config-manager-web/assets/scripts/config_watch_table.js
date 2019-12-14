var data = {
    context: ""
}

var harvestApp = new Vue({
    el: '#harvest-context',
    data: data
})

$(function(){
    $('#config_watch_table').bootstrapTable({
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
            title: '编号',
        }, {
            field: 'clientId',
            title: '客户端标识',
        }, {
            field: 'address',
            title: '客户端IP',
        }, {
            field: 'lastMd5',
            title: '最新配置签名',
        }, {
            field: 'watchType',
            title: '监听方式'
        }],
        data:[],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/receive_file',
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
        onEditableSave: function (field, row, oldValue, $el) {
        }
    })
})
