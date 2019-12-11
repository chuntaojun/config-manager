function orderBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#order-plan" style="margin-right:15px;">查看详细</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        $('#plan-id').val(row['plan_id'])
        $('#item-id').val(row['item_id'])
        $('#item-name').val(row['item_name'])
        $('#purchase-quantity').val(row['count'])
        $('#unit-price').val(row['unit_price'])
        $('#total-price').val(row['total_money'])
        $('#supplier_id').val(row['supplier_id'])
        $('#approval-status').val(row['state'])
        if (row['state'] == 1 || row['state'] == '1') {
            $('#submit-require-order').attr("disabled","disabled")
        }
    },
}

$(function(){
    $('#purchase-plan-table').bootstrapTable({
        classes: 'table table-hover',
        height: undefined,
        undefinedText: '-',
        sortName: undefined,
        sortOrder: 'asc',
        striped: true,
        editable: true,
        columns: [{
            field: 'plan_id',
            title: '采购计划单编号',
        }, {
            field: 'item_id',
            title: '物品编号',
        }, {
            field: 'item_name',
            title: '物品名称',
            visible: false
        }, {
            field: 'approval_staff_id',
            title: '审批员工编号',
        }, {
            field: 'unit_price',
            title: '单价',
            visible: false
        }, {
            field: 'total_money',
            title: '采购总金额',
            visible: false
        }, {
            field: 'count',
            title: '采购数量',
            visible: false
        }, {
            field: 'create_date',
            title: '申报日期'
        }, {
            field: 'supplier_id',
            title: '供应商编号'
        }, {
            field: 'approval_date',
            title: '审批日期',
            visible: false
        }, {
            field: 'demand_ids',
            title: '采购需求单编号',
            visible: false
        }, {
            field: 'state',
            title: '审批状态'
        }, {
            field: 'salary_amout',
            title: '详细',
            events: operateEvents,
            formatter: orderBtn
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/plan',
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
        uniqued: 'plan_id',
        idField: 'plan_id',
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
