# 错误处理记录
## 4.15
1. 使用fastjson进行序列化和反序列操作, 当属性有Class参数的时候, 由JsonByte转化为Bean的时候需要在parse.Object 中加入JSONReader.Feature.SupportClassForName参数

```java
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T object = JSON.parseObject(bytes, type, JSONReader.Feature.SupportClassForName); // 正常运行
        // T object = JSON.parseObject(bytes, type); 报错
        if (object instanceof RpcRequest){
            return handleRequest((RpcRequest) object, type);
        }
        if (object instanceof RpcResponse){
            return handleResponse((RpcResponse) object, type);
        }
        return object;
    }
```
2. YamlUnit 和 BeanUnit, 将设置文件转为bean对象处理方法
3. 双检锁单例模式实现