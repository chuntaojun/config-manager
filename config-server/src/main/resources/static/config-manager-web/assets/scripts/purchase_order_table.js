function orderBtn(value, row, index) {
    if (row['state'] !== '采购完成') {}
    return [
        '<button class="LookDetail btn btn-info btn-sm" data-toggle="modal" data-target="#order-detail">详细</button>',
        '<button class="HarvestWrite btn btn-success btn-sm" data-toggle="modal" data-target="#harvest" style="margin-left:10px">填写收货单</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        $('#order-id').val(row['order_id'])
        $('#state').val(row['state'])
        $('#item-id').val(row['item_id'])
        $('#item-name').val(row['item_name'])
        $('#supplier-id').val(row['supplier_id'])
        $('#pruchase-count').val(row['pruchase_count'])
        $('#accepted-count').val(row['accepted_count'])
        $('#unit-price').val(row['unit_price'])
        $('#total-price').val(row['total_price'])
        $('#plan-id').val(row['plan_id'])
    },
    'click .HarvestWrite': function(e, value, row, index) {
        console.log(row)
        $('#har-order-id').val(row['order_id'])
        $('#har-item-id').val(row['item_id'])
    }
}

$(function(){
    $('#purchase-order-table').bootstrapTable({
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
            field: 'order_id',
            title: '采购订单号',
            width: '20%',
        }, {
            field: 'state',
            title: '订单状态',
            width: '20%',
        }, {
            field: 'item_name',
            title: '物品名称',
            width: '20%',
        }, {
            field: 'total_price',
            title: '总价',
            width: '20%',
        }, {
            field: 'item_id',
            title: '物品编号',
            visible: false
        }, {
            field: 'supplier_id',
            title: '供应商编号',
            visible: false
        }, {
            field: 'pruchase_count',
            title: '采购数量',
            visible: false
        }, {
            field: 'accepted_count',
            title: '接受数量',
            visible: false
        }, {
            field: 'unit_price',
            title: '单价',
            visible: false
        }, {
            field: 'plan_id',
            title: '采购计划单号',
            visible: false
        }, {
            field: 'salary_amout',
            title: '详细',
            width: '20%',
            events: operateEvents,
            formatter: orderBtn
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/order',
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
        uniqued: 'order_id',
        idField: 'order_id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})
