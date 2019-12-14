$(function(){
    $('#config_table').bootstrapTable({
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
            field: 'groupId',
            title: 'GroupId',
        }, {
            field: 'dataId',
            title: 'DataId',
        }, {
            field: 'type',
            title: 'ConfigType',
        }, {
            field: 'details',
            title: '详细',
            events: operateEvents,
            formatter: requireFileBtn,
        }],
        data:[],
        method: 'get',
        url: HTTP_REQUEST_API_URL + '/api/get/item',
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
            var data = {
                "id": row['id'],
                "description": row['description'],
                "name": row['name'],
                "type": row['type']
            }
            $.ajax({
                url: HTTP_REQUEST_API_URL + '/api/update/item',
                type: 'post',
                data: JSON.stringify(data),
                dataType: 'json',
                headers: {
                    "Content-Type": "Application/json",
                    "token": sessionStorage.getItem("access_token")
                },
                success: function(result) {
                    alert(result.msg)
                }
            })
        }
    })
})
