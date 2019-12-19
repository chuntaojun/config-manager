function requireBtn(value, row, index) {
    return [
        '<button class="LookDetail btn btn-info" data-toggle="modal" data-target="#resource_access" style="margin-right:15px;">可访问资源</button>'
    ].join('');
}

window.operateEvents = {
    'click .LookDetail': function (e, value, row, index) {
        editor.txt.html(row['resources'])
    },
}

$(function () {
    $('#user_table').bootstrapTable({
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
            title: '用户编号',
        }, {
            field: 'username',
            title: '用户名',
        }, {
            field: 'role',
            title: '角色',
        }, {
            field: 'enableResource',
            title: '可访问资源',
            events: operateEvents,
            formatter: requireBtn,
        }],
        data: [],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/v1/allUser',
        cache: true,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"config-manager-token": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            params['username'] = $('#user_name').val()
            return params;
        },
        pagination: true,
        sidePagination: 'server', // client or server
        pageNumber: 1,
        pageSize: 10,
        selectItemName: 'btSelectItem',
        showHeader: true,
        showColumns: true,
        showRefresh: true,
        showToggle: true,
        smartDisplay: false,
        minimumCountColumns: 1,
        uniqued: 'id',
        idField: 'id',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar_seller',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
    })
})
