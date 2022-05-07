package org.sorapointa.proto

import org.junit.jupiter.api.Test

class ProtoTest {
    @Test
    fun proto() {
        abilityAppliedAbility {
            abilityName = abilityString {
                str = "114514"
            }
            abilityOverride = abilityString {
                hash = 114514
            }
            overrideMap.apply {
                add(
                    abilityScalarValueEntry {
                        floatValue = 1.3f
                        key = abilityString {
                            str = "1919810"
                        }
                        valueType = AbilityScalarTypeOuterClass.AbilityScalarType.FLOAT
                    }
                )
            }
            instancedAbilityId = 100
        }.toByteString()
    }
}
