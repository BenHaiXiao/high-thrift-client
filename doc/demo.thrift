namespace java com.github.xiaobenhai.thrift.client.test.protocol

service  DemoService{
/**
*ping验证服务是否正常
**/
i64 ping(),
/**
*正常逻辑
**/
string  test(1: string value )
}



