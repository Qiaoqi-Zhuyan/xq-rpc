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

## 4.16
1. 修改spiloader的实现, 支持不同的spi接口的返回
2. etcd无法请求, provide的http handle中需要将service放在本地的注册中, etcd请求会获取本地的注册中的实现类中

## 4.17 
1. 使用ConcurrentHashSet线程安全的set
2. 对于list的判断
```java
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null){
            return cachedServiceMetaInfoList;
        }
```
应该判断null, 不能用isEmpty判断

## 4.22
出现无法通过tcp获取实例, 原因在`ProtocolMessageDecoder.java` 中, 在对消息进行编码的时候
```java
header.setBodyLength(buffer.getInt(13));
```
这行代码错写成
```java
header.setBodyLength(Buffer.buffer().getInt(13));
```
自动补全的时候一直按tab, 没有去检查这部分代码

## 4.23
使用注解驱动，无法对服务进行注入

原因是在`RpcReference`中缺少注解
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
```
这里
```java
Retention(RetentionPolicy.Class)
```
这样子些是错误的, 对springboot-start注解理解不深