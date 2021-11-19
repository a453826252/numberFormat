class NumberFormat {
    private var upcases = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "点")
    private val unit = arrayOf("拾", "佰", "仟", "万")
    private val unit2 = arrayOf(unit[3], "亿")
    fun encode(number: Double): String {
        if (number > 9999999999999999 || number < -9999999999999999) {
            throw IllegalArgumentException("最多支持-9999999999999999~9999999999999999（16位数字）之间的数字转换")
        }
        var numstr = number.toString().reversed()
        //正负数，正不显示，负显示
        var numberProperty = ""
        if (number < 0) {
            numstr = numstr.dropLast(1) //去掉负号
            numberProperty = "负"
        }
        val pointIndex = numstr.indexOfFirst { it == '.' }
        var pointUpCaseString = ""
        if(pointIndex != -1){
            val pointStr =numstr.take(pointIndex+1)
            pointUpCaseString = processPoint(pointStr)
            numstr = numstr.drop(pointIndex+1)
        }
        return if (numstr.length <= 8) {
            (pointUpCaseString + numberProcess_8(numstr)).reversed()
        } else {
            val first8 = numstr.take(8)
            val last = numstr.drop(8)
            val first4Upcase = numberProcess_8(first8)
            val lastUpcase = numberProcess_8(last)
            //整数部分分为低8位和高8位，转换后，用“亿”连接
            if (first8.takeLast(1) != "0" && last.take(1) == upcases[0]) {
                (pointUpCaseString + first4Upcase + "${upcases[0]}${unit2[1]}" + lastUpcase + numberProperty).reversed()
            } else {
                (pointUpCaseString + first4Upcase + unit2[1] + lastUpcase + numberProperty).reversed()
            }
        }

    }

    fun setNumberUpCase(index: Int, upCase: String): NumberFormat {
        if (index < 0 || index > 9) {
            throw IllegalArgumentException("设置数字对应的大写汉字时，index：0~10，分别对应：零~玖,10->点")
        }
        upcases[index] = upCase
        return this
    }

    fun setNumberUpCase(upcases: Array<String>): NumberFormat {
        if (upcases.size < 10) {
            throw IllegalArgumentException("设置数字的大写汉字数组的长度必须大于等于10")
        }
        this.upcases = upcases
        return this
    }

    fun setUnit(pow: Int, unit: String): NumberFormat {
        if (pow < 1 || (pow > 4 && pow !=8)) {
            throw IllegalArgumentException("设置单位时，pow取值：1~4，分别对应：拾~万,8对应亿")
        }
        if(pow == 8){
            this.unit2[1] = unit
        }else{
            this.unit[pow - 1] = unit
        }
        return this
    }

    /**
     * 将8位整数字转为大写。
     * 将8位数字分为低4位和高4位，然后利用numberProcess_4得到4位数字的大写汉字，然后将低4位和高4位转换的大写汉字使用“万”连接起来。
     * 如：876543210,->[8765,3210]
     * 8765->八千柒佰陆拾伍
     * 3210->叁仟贰佰壹拾
     * 连接->八千柒佰陆拾伍万叁仟贰佰壹拾
     */
    private fun numberProcess_8(number: String): String {
        if (number.length > 8) {
            throw IllegalArgumentException("numberProcess_8 can only process the number with length under(include) 8")
        }
        if (number.length <= 4) {
            return numberProcess_4(number)
        } else {
            val first4 = number.take(4)
            val last = number.drop(4)
            val first4Upcase = numberProcess_4(first4)
            val lastUpcase = numberProcess_4(last)
            //因为numberProcess_4函数保证了返回值开头不会有零，末尾可能有零。所以当实际（12340074）低4位末尾没有零（1234），而高4位开头有零时（0074），需要手动添加一个零
            if (first4.takeLast(1) != "0" && last.take(1) == "0") {
                return first4Upcase + "${upcases[0]}${unit2[0]}" + lastUpcase
            } else {
                return first4Upcase + unit2[0] + lastUpcase
            }
        }

    }

    /**
     * 处理4位整数数及以下的数字转换，规则：将数字转换为大写后，后面紧跟着一个单位，同时满足：
     * 1. 单位前面的数字不能为零，如 贰仟零佰零一
     * 2. 转换后不能连续出现两个及以上的零，如贰仟零零一
     * 3。最后一位不能为零，如 贰仟零贰拾零
     * 4. 第一位不能是单位，如万贰仟零贰拾
     * @param number 反转后的数字，例如4700，反转后是0074
     * @return 转换后的大写数字（倒序），结尾可能会有零也可能没有，开头绝对没有零。如：拾贰佰贰零
     */
    private fun numberProcess_4(number: String): String {
        if (number.length > 4 || number.isEmpty()) {
            throw IllegalArgumentException("numberProcess_4 can only process the number with length under(include) 4")
        }
        val builder = StringBuilder()
        var unitIndex = 0
        var numbervar = number
        while (numbervar.isNotEmpty()) {
            val num = numbervar.take(1)
            //如果当前数字是零，则删除builder中的上一个字符（上一个字符是单位）
            if (num == "0" && builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
            //保证不会出现两个以上连续的零
            if (num != "0" || builder.takeLast(1) != upcases[0]) {
                builder.append(upcases[num.toInt()])
            }
            //保证最后一位不是单位（反转后就是第一位）
            if (numbervar.length >= 2) {
                builder.append(unit[unitIndex++])
            }
            numbervar = numbervar.drop(1)
        }
        //保证第一位不是零（反转后就是最后一位）
        if (builder.take(1) == upcases[0]) {
            builder.deleteCharAt(0)
        }
        return builder.toString()
    }

    private fun processPoint(pointStr:String):String{
        val builder = StringBuilder()
        var pointStrVar = pointStr
        while (pointStrVar.isNotEmpty()){
            val ch = pointStrVar.take(1)
            if(ch == "."){
                builder.append(upcases[10])
            }else{
                builder.append(upcases[ch.toInt()])
            }
            pointStrVar = pointStrVar.drop(1)
        }
        //去掉结尾的零
        while(builder.isNotEmpty()){
            if(builder.take(1) == upcases[0]){
                builder.deleteCharAt(0)
            }else{
                break
            }
        }
        //第一位是“点”,说明点后面没有数字了，此时不翻译点
        if(builder.take(1) == upcases[10]){
            return ""
        }
        return builder.toString()
    }

}

