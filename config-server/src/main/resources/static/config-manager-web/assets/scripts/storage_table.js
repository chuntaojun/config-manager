function storageBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#myModal" style="margin-right:15px;">对应采购订单</button>'
    ].join('');
}

var data = {
    order_id: "",
    item_id: "",
    item_name: "",
    unit_price: "",
    total_price: "",
    pruchase_count: "",
    state: "",
    supplier_id: ""
}

var app = new Vue({
    el: '#order_detail_info',
    data: data
})

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        var ddNo = row['order_id']
        $.ajax({
            url: HTTP_REQUEST_API_URL + '/api/get/order?search=' + ddNo,
            type: 'get',
            dataType: 'json',
            headers: {
                "Content-Type": "Application/json",
                "token": sessionStorage.getItem("access_token")
            },
            success: function (result) {
                if (result.length == 0) {
                    alert("err")
                } else {
                    var orderEntry = result[0]
                    data.order_id = orderEntry.order_id
                    data.item_id = orderEntry.item_id
                    data.item_name = orderEntry.item_name
                    data.unit_price = orderEntry.unit_price
                    data.total_price = orderEntry.total_price
                    data.pruchase_count = orderEntry.pruchase_count
                    data.state = orderEntry.state
                    data.supplier_id = orderEntry.supplier_id
                }
            }
        })
    },
}

$(function () {
    $('#storage-table').bootstrapTable({
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
            field: 'report_id',
            title: '入库编号',
        }, {
            field: 'order_id',
            title: '采购订单号',
        }, {
            field: 'item_id',
            title: '入库物品编号'
        }, {
            field: 'date',
            title: '入库日期',
        }, {
            field: 'count',
            title: '入库数量',
        }, {
            field: 'look-detail',
            title: '查看详细',
            events: operateEvents,
            formatter: storageBtn
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/entry',
        cache: true,
        contentType: 'application/json',
        ajaxOptions: {
            headers: {
                "token": sessionStorage.getItem("access_token")
            }
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
        uniqued: 'report_id',
        idField: 'report_id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})