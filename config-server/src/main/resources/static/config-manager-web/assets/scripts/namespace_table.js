function requireFileBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#namespace_detail" style="margin-right:15px;">查看详细</button>',
        '<button class="Delete btn btn-error" onclick="deleteNamespace()" style="margin-right:15px;">删除</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        const namespaceId = row['namespaceId']
        $.ajax({
            url: HTTP_REQUEST_API_URL + '/api/v1/namespace/owner?namespaceId=' + namespaceId,
            type: 'GET',
            processData: false,
            headers: {
                "Content-Type": 'application/json;charset=utf-8'
            },
            contentType: 'application/json;charset=utf-8',
            success: function (response) {
                const result = response
                if (Number(result['code']) === 200) {
                    const users = result['data']
                    let str = '';
                    for (let i = 0; i < users.length; i ++) {
                        str += users[i] + ','
                    }
                    $('#owners').val(str)
                } else {
                    alert(result['errMsg'])
                }
            }
        })
    },

    'click .Delete': function (e, value, row, index) {
        const namespaceId = row['namespaceId']
        $.ajax({
            url: HTTP_REQUEST_API_URL + '/api/v1/namespace/delete?namespaceId=' + namespaceId,
            type: 'DELETE',
            processData: false,
            headers: {
                "Content-Type": 'application/json;charset=utf-8'
            },
            contentType: 'application/json;charset=utf-8',
            success: function (response) {
                const result = response
                if (Number(result['code']) === 200) {
                    $('#namespace-table').bootstrapTable('refresh');
                } else {
                    alert(result['errMsg'])
                }
            }
        })
    }
}


$(function() {
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
        url: HTTP_REQUEST_API_URL + '/api/v1/namespace/all',
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
        pageList: [10, 25, 50],
        search: true,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: false,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        idField: 'namespaceId',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar_seller',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})
