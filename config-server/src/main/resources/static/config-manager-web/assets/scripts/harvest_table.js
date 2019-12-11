var data = {
    context: ""
}

var harvestApp = new Vue({
    el: '#harvest-context',
    data: data
})

$(function(){
    $('#harvest-table').bootstrapTable({
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
            title: '收货单文件编号',
        }, {
            field: 'order_id',
            title: '采购订单号',
        }, {
            field: 'item_id',
            title: '收获物品编号',
        }, {
            field: 'receive_count',
            title: '签收数量',
        }, {
            field: 'receive_staff_id',
            title: '签收员工工号'
        }, {
            field: 'receive_date',
            title: '签收日期',
        }, {
            field: 'content',
            title: '内容',
            visible: true,
            formatter: function(value, row, index) {
                data.context = value
                return '<button class="btn btn-info" data-toggle="modal" data-target="#harvest-context">查看内容</button>'
            }
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
