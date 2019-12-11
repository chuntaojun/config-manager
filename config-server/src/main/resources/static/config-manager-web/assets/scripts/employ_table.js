$(function(){
    $('#employ-table').bootstrapTable({
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
            field: 'salary_uuid',
            title: '员工编号',
        }, {
            field: 'expresser_name',
            title: '员工姓名',
        }, {
            field: 'salary_application_time',
            title: '员工职位',
        }, {
            field: 'expresser_balance',
            title: '所属部门',
        }, {
            field: 'salary_amout',
            title: '上级领导',
        }],
        data: [],
        method: 'get',
        url: 'http://115.159.3.213:8089/api/su-di/admin/' + sessionStorage.getItem("admin_id") + '/courier/salarys',
        cache: true,
        contentType: 'application/json',
        ajaxOptions:{
            headers: {"Authorization": sessionStorage.getItem("access_token")}
        },
        queryParams: function (params) {
            return params;
        },
        queryParamsType: 'limit', // undefined
        responseHandler: function (res) {
            console.log(JSON.stringify(res.m))
            return {
                "total": res.m,
                "rows": res.data
            }
        },
        pagination: true,
        sidePagination: 'server', // client or server
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
        uniqued: 'salary_uuid',
        idField: 'salary_uuid',
        cardView: false,
        clickToSelect: false,
        singleSelect: false,
        toolbar: '#toolbar',
        checkboxHeader: true,
        sortable: true,
        maintainSelected: false,
        onEditableSave: function (field, row, oldValue, $el) {
            $.ajax({
                type: 'post',
                url: '/admin/edit/Merchant?ID=' + row['no'],
                data: JSON.stringify(row),
                dataType: 'JSON',
                contentType: "application/json;charset=UTF-8",
                headers: {
					"Content-Type": "Application/json",
					"token": sessionStorage.getItem("access_token")
				},
                success: function (data) {
                    if (!data.judge)
                        alert("修改失败,请联系后台人员")
                    if (data.judge)
                        alert("更改成功")
                }
            });
        }
    })
})
